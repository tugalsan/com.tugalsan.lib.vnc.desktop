// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package com.tugalsan.lib.vnc.desktop.server.rfb.protocol;

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.CursorPosDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.CopyRectDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.ZlibDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.TightDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.RREDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.HextileDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.Decoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.RichCursorDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.DesctopSizeDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.RawDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.ByteBuffer;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.ZRLEDecoder;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.ClientToServerMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.FramebufferUpdateRequestMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.SetPixelFormatMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.ClientMessageType;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.SetEncodingsMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRfbSessionListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.RfbCapabilityInfo;
import com.tugalsan.lib.vnc.desktop.server.rfb.ClipboardController;
import com.tugalsan.lib.vnc.desktop.server.rfb.IChangeSettingsListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRequestString;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRepaintController;
import com.tugalsan.lib.vnc.desktop.server.exceptions.AuthenticationFailedException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.UnsupportedProtocolVersionException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.UnsupportedSecurityTypeException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.FatalException;
import com.tugalsan.lib.vnc.desktop.server.core.SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.EncodingType;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.PixelFormat;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.handlers.Handshaker;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.tunnel.TunnelType;
import com.tugalsan.lib.vnc.desktop.server.transport.BaudrateMeter;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;
import com.tugalsan.api.thread.server.async.TS_ThreadAsync;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import java.util.logging.Logger;

public class Protocol implements IChangeSettingsListener {

    private final ProtocolContext context;
    private static final Logger logger = Logger.getLogger(Protocol.class.getName());
    private final IRequestString passwordRetriever;
    private MessageQueue messageQueue;
    private SenderTask senderTask;
    private ReceiverTask receiverTask;
    private IRfbSessionListener rfbSessionListener;
    private IRepaintController repaintController;
    private Thread senderThread;
    private Thread receiverThread;
    private PixelFormat serverPixelFormat;

    private final Map<EncodingType, Decoder> decoders = new LinkedHashMap();
    private final Set<ClientMessageType> clientMessageTypes = new HashSet();
    private boolean inCleanUp = false;
    private boolean isMac;
    private BaudrateMeter baudrateMeter;
    private IRequestString connectionIdRetriever;

    public Protocol(Transport transport, IRequestString passwordRetriever, ProtocolSettings settings) {
        context = new ProtocolContext();
        context.transport = transport;
        this.passwordRetriever = passwordRetriever;
        context.settings = settings;
        decoders.put(EncodingType.RAW_ENCODING, RawDecoder.getInstance());
    }

    public void handshake() throws UnsupportedProtocolVersionException, UnsupportedSecurityTypeException,
            AuthenticationFailedException, TransportException, FatalException {
        logger.info("Starting handshake...");
        context.transport = new Handshaker(this).handshake(getTransport());
        logger.info("context.transport:%s".formatted(context.transport.toString()));
        messageQueue = new MessageQueue(); // TODO Why here?
        logger.info("messageQueue:%s".formatted(messageQueue.toString()));
    }

    public IRequestString getPasswordRetriever() {
        return passwordRetriever;
    }

    /**
     * Following the server initialisation message it's up to the client to send
     * whichever protocol messages it wants. Typically it will send a
     * SetPixelFormat message and a SetEncodings message, followed by a
     * FramebufferUpdateRequest. From then on the server will send
     * FramebufferUpdate messages in response to the client's
     * FramebufferUpdateRequest messages. The client should send
     * FramebufferUpdateRequest messages with incremental set to true when it
     * has finished processing one FramebufferUpdate and is ready to process
     * another. With a fast client, the rate at which FramebufferUpdateRequests
     * are sent should be regulated to avoid hogging the network.
     */
    public void startNormalHandling(TS_ThreadSyncTrigger killTrigger, IRfbSessionListener rfbSessionListener,
            IRepaintController repaintController, ClipboardController clipboardController) {
        this.rfbSessionListener = rfbSessionListener;
        this.repaintController = repaintController;
//		if (settings.getColorDepth() == 0) {
//			settings.setColorDepth(pixelFormat.depth); // the same the server sent when not initialized yet
//		}
        correctServerPixelFormat();
        context.setPixelFormat(createPixelFormat(context.settings));
        sendMessage(new SetPixelFormatMessage(context.pixelFormat));
        logger.fine("sent: %s".formatted(context.pixelFormat.toString()));

        sendSupportedEncodingsMessage(context.settings);
        context.settings.addListener(Protocol.this); // to support pixel format (color depth), and encodings changes
        context.settings.addListener(repaintController);

        sendRefreshMessage();
        senderTask = new SenderTask(killTrigger, messageQueue, context.transport, Protocol.this);
        senderThread = TS_ThreadAsync.now(killTrigger, kt -> senderTask.run());
        resetDecoders();
        receiverTask = new ReceiverTask(killTrigger,
                context.transport, repaintController,
                clipboardController,
                Protocol.this, baudrateMeter);
        receiverThread = TS_ThreadAsync.now(killTrigger, kt -> receiverTask.run());
    }

    private void correctServerPixelFormat() {
        // correct true color flag
        if (0 == serverPixelFormat.trueColourFlag) {
            //we don't support color maps, so always set true color flag up
            //and select closest convenient value for bpp/depth
            int depth = serverPixelFormat.depth;
            if (0 == depth) {
                depth = serverPixelFormat.bitsPerPixel;
            }
            if (0 == depth) {
                depth = 24;
            }
            if (depth <= 3) {
                serverPixelFormat = PixelFormat.create3bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 6) {
                serverPixelFormat = PixelFormat.create6bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 8) {
                serverPixelFormat = PixelFormat.create8bitColorDepthBGRPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 16) {
                serverPixelFormat = PixelFormat.create16bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else {
                serverPixelFormat = PixelFormat.create24bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            }
        }
        // correct .depth to use actual depth 24 instead of incorrect 32, used by ex. UltraVNC server, that cause
        // protocol incompatibility in ZRLE encoding
        final long significant = serverPixelFormat.redMax << serverPixelFormat.redShift
                | serverPixelFormat.greenMax << serverPixelFormat.greenShift
                | serverPixelFormat.blueMax << serverPixelFormat.blueShift;
        if (32 == serverPixelFormat.bitsPerPixel
                && ((significant & 0x00ff000000L) == 0 || (significant & 0x000000ffL) == 0)
                && 32 == serverPixelFormat.depth) {
            serverPixelFormat.depth = 24;
        }
    }

    public void sendMessage(ClientToServerMessage message) {
        messageQueue.put(message);
    }

    public void sendSupportedEncodingsMessage(ProtocolSettings settings) {
        final LinkedHashSet<EncodingType> encodings = new LinkedHashSet();
        final var preferredEncoding = settings.getPreferredEncoding();
        if (preferredEncoding != EncodingType.RAW_ENCODING) {
            encodings.add(preferredEncoding); // preferred first
        }
        decoders.keySet().stream()
                .filter(e -> !(e == preferredEncoding))
                .forEachOrdered(e -> {
                    switch (e) {
                        case RAW_ENCODING -> {
                        }
                        case COMPRESS_LEVEL_0 -> {
                            final var compressionLevel = settings.getCompressionLevel();
                            if (compressionLevel > 0 && compressionLevel < 10) {
                                encodings.add(EncodingType.byId(EncodingType.COMPRESS_LEVEL_0.getId() + compressionLevel));
                            }
                        }
                        case JPEG_QUALITY_LEVEL_0 -> {
                            final var jpegQuality = settings.getJpegQuality();
                            final var colorDepth = settings.getColorDepth();
                            if (jpegQuality > 0 && jpegQuality < 10
                                    && (colorDepth == ProtocolSettings.COLOR_DEPTH_24
                                    || colorDepth == ProtocolSettings.COLOR_DEPTH_SERVER_SETTINGS)) {
                                encodings.add(EncodingType.byId(EncodingType.JPEG_QUALITY_LEVEL_0.getId() + jpegQuality));
                            }
                        }
                        case COPY_RECT -> {
                            if (settings.isAllowCopyRect()) {
                                encodings.add(EncodingType.COPY_RECT);
                            }
                        }
                        case RICH_CURSOR -> {
                            if (settings.getMouseCursorTrack() == LocalPointer.HIDE
                                    || settings.getMouseCursorTrack() == LocalPointer.ON) {
                                encodings.add(EncodingType.RICH_CURSOR);
                            }
                        }
                        case CURSOR_POS -> {
                            if (settings.getMouseCursorTrack() == LocalPointer.HIDE
                                    || settings.getMouseCursorTrack() == LocalPointer.ON) {
                                encodings.add(EncodingType.CURSOR_POS);
                            }
                        }
                        default ->
                            encodings.add(e);
                    }
                });
        var encodingsMessage = new SetEncodingsMessage(encodings);
        sendMessage(encodingsMessage);
        logger.fine("sent: %s".formatted(encodingsMessage.toString()));
    }

    /**
     * create pixel format by bpp
     */
    private PixelFormat createPixelFormat(ProtocolSettings settings) {
        var serverBigEndianFlag = serverPixelFormat.bigEndianFlag;
        return switch (settings.getColorDepth()) {
            case ProtocolSettings.COLOR_DEPTH_24 ->
                PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
            case ProtocolSettings.COLOR_DEPTH_16 ->
                PixelFormat.create16bitColorDepthPixelFormat(serverBigEndianFlag);
            case ProtocolSettings.COLOR_DEPTH_8 ->
                hackForMacOsXScreenSharingServer(PixelFormat.create8bitColorDepthBGRPixelFormat(serverBigEndianFlag));
            case ProtocolSettings.COLOR_DEPTH_6 ->
                hackForMacOsXScreenSharingServer(PixelFormat.create6bitColorDepthPixelFormat(serverBigEndianFlag));
            case ProtocolSettings.COLOR_DEPTH_3 ->
                hackForMacOsXScreenSharingServer(PixelFormat.create3bitColorDepthPixelFormat(serverBigEndianFlag));
            case ProtocolSettings.COLOR_DEPTH_SERVER_SETTINGS ->
                serverPixelFormat;
            default ->
                PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
        }; // unsupported bpp, use default
    }

    private PixelFormat hackForMacOsXScreenSharingServer(PixelFormat pixelFormat) {
        if (isMac) {
            pixelFormat.bitsPerPixel = pixelFormat.depth = 16;
        }
        return pixelFormat;
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        var settings = (ProtocolSettings) e.getSource();
        if (settings.isChangedEncodings()) {
            sendSupportedEncodingsMessage(settings);
        }
        if (settings.isChangedColorDepth() && receiverTask != null) {
            receiverTask.queueUpdatePixelFormat(createPixelFormat(settings));
        }
    }

    public void sendRefreshMessage() {
        sendMessage(new FramebufferUpdateRequestMessage(0, 0, context.fbWidth, context.fbHeight, false));
        logger.fine("sent: full FB Refresh");
    }

    public void sendFbUpdateMessage() {
        sendMessage(receiverTask.fullscreenFbUpdateIncrementalRequest);
    }

    public void cleanUpSession(String message) {
        cleanUpSession();
        rfbSessionListener.rfbSessionStopped(message);
    }

    public void cleanUpSession() {
        synchronized (this) {
            if (inCleanUp) {
                return;
            }
            inCleanUp = true;
        }
        if (senderTask != null && senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if (receiverTask != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
        if (senderTask != null) {
            try {
                senderThread.join(1000);
            } catch (InterruptedException e) {
                // nop
            }
            senderTask = null;
        }
        if (receiverTask != null) {
            try {
                receiverThread.join(1000);
            } catch (InterruptedException e) {
                // nop
            }
            receiverTask = null;
        }
        synchronized (this) {
            inCleanUp = false;
        }
        ByteBuffer.removeInstance();
    }

    public void setServerPixelFormat(PixelFormat serverPixelFormat) {
        this.serverPixelFormat = serverPixelFormat;
    }

    public ProtocolSettings getSettings() {
        return context.getSettings();
    }

    public Transport getTransport() {
        return context.getTransport();
    }

    public int getFbWidth() {
        return context.getFbWidth();
    }

    public void setFbWidth(int frameBufferWidth) {
        context.setFbWidth(frameBufferWidth);
    }

    public int getFbHeight() {
        return context.getFbHeight();
    }

    public void setFbHeight(int frameBufferHeight) {
        context.setFbHeight(frameBufferHeight);
    }

    public PixelFormat getPixelFormat() {
        return context.getPixelFormat();
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        context.setPixelFormat(pixelFormat);
        if (repaintController != null) {
            repaintController.setPixelFormat(pixelFormat);
        }
    }

    public void setRemoteDesktopName(String name) {
        context.setRemoteDesktopName(name);
    }

    public String getRemoteDesktopName() {
        return context.getRemoteDesktopName();
    }

    public void setTight(boolean isTight) {
        context.setTight(isTight);
    }

    public boolean isTight() {
        return context.isTight();
    }

    public void setProtocolVersion(Handshaker.ProtocolVersion protocolVersion) {
        context.setProtocolVersion(protocolVersion);
    }

    public Handshaker.ProtocolVersion getProtocolVersion() {
        return context.getProtocolVersion();
    }

    public void registerRfbEncodings() {
        decoders.put(EncodingType.TIGHT, new TightDecoder());
        decoders.put(EncodingType.HEXTILE, new HextileDecoder());
        decoders.put(EncodingType.ZRLE, new ZRLEDecoder());
        decoders.put(EncodingType.ZLIB, new ZlibDecoder());
        decoders.put(EncodingType.RRE, new RREDecoder());
        decoders.put(EncodingType.COPY_RECT, new CopyRectDecoder());

        decoders.put(EncodingType.RICH_CURSOR, new RichCursorDecoder());
        decoders.put(EncodingType.DESKTOP_SIZE, new DesctopSizeDecoder());
        decoders.put(EncodingType.CURSOR_POS, new CursorPosDecoder());
    }

    public void resetDecoders() {
        decoders.values().stream()
                .filter(decoder -> (decoder != null))
                .forEachOrdered(decoder -> {
                    decoder.reset();
                });
    }

    public Decoder getDecoderByType(EncodingType type) {
        return decoders.get(type);
    }

    public void registerEncoding(RfbCapabilityInfo capInfo) {
        try {
            final var encodingType = EncodingType.byId(capInfo.getCode());
            if (!decoders.containsKey(encodingType)) {
//                final Decoder decoder = encodingType.klass.newInstance();
                final var decoder = encodingType.klass.getDeclaredConstructor().newInstance();
                if (decoder != null) {
                    decoders.put(encodingType, decoder);
                    logger.finer("Register encoding: %s".formatted(encodingType.toString()));
                }
            }
        } catch (IllegalArgumentException e) {
            logger.finer(e.getMessage());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            logger.warning(e.getMessage());
        }
    }

    public void registerClientMessageType(RfbCapabilityInfo capInfo) {
        try {
            final var clientMessageType = ClientMessageType.byId(capInfo.getCode());
            clientMessageTypes.add(clientMessageType);
            logger.finer("Register client message type: %s".formatted(clientMessageType.toString()));
        } catch (IllegalArgumentException e) {
            logger.finer(e.getMessage());
        }
    }

    /**
     * Check whether server is supported for given client-to-server message
     *
     * @param type client-to-server message type to check for
     * @return true when supported
     */
    public boolean isSupported(ClientMessageType type) {
        return clientMessageTypes.contains(type) || ClientMessageType.isStandardType(type);
    }

    public void setTunnelType(TunnelType tunnelType) {
        context.setTunnelType(tunnelType);
    }

    public TunnelType getTunnelType() {
        return context.getTunnelType();
    }

    public void setMac(boolean isMac) {
        this.isMac = isMac;
    }

    public void setBaudrateMeter(BaudrateMeter baudrateMeter) {
        this.baudrateMeter = baudrateMeter;
    }

    public int kBPS() {
        return baudrateMeter == null ? -1 : baudrateMeter.kBPS();
    }

    public boolean isMac() {
        return isMac;
    }

    public void setConnectionIdRetriever(IRequestString connectionIdRetriever) {
        this.connectionIdRetriever = connectionIdRetriever;
    }

    public IRequestString getConnectionIdRetriever() {
        return connectionIdRetriever;
    }
}
