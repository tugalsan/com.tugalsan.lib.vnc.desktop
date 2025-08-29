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
package com.tugalsan.lib.vnc.desktop.server.rfb;

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_AuthenticationFailed;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedProtocolVersion;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedSecurityType;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Fatal;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopCore_SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_BaudrateMeter;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;
import com.tugalsan.api.thread.server.async.run.TS_ThreadAsyncRun;
import com.tugalsan.api.thread.server.async.await.TS_ThreadAsyncAwait;
import com.tugalsan.api.function.client.maythrowexceptions.checked.TGS_FuncMTCUtils;
import com.tugalsan.api.function.client.maythrowexceptions.unchecked.TGS_FuncMTUUtils;
import com.tugalsan.api.log.server.TS_Log;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import java.util.logging.Logger;

public class TS_LibVncDesktopRfbProtocol_Protocol implements TS_LibVncDesktopRfb_IChangeSettingsListener {

    final private static TS_Log d = TS_Log.of(false, TS_LibVncDesktopRfbProtocol_Protocol.class);
    private final TS_LibVncDesktopRfbProtocol_Context context;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopRfbProtocol_Protocol.class.getName());
    private final TS_LibVncDesktopRfb_IRequestString passwordRetriever;
    private TS_LibVncDesktopRfbProtocol_MessageQueue messageQueue;
    private TS_LibVncDesktopRfbProtocol_SenderTask senderTask;
    private TS_LibVncDesktopRfbProtocol_ReceiverTask receiverTask;
    private TS_LibVncDesktopRfb_ISessionListener rfbSessionListener;
    private TS_LibVncDesktopRfb_IRepaintController repaintController;
    private Thread senderThread;
    private Thread receiverThread;
    private TS_LibVncDesktopRfbEncoding_PixelFormat serverPixelFormat;

    private final Map<TS_LibVncDesktopRfbEncoding_Type, TS_LibVncDesktopRfbEncoding_Decoder> decoders = new LinkedHashMap();
    private final Set<TS_LibVncDesktopRfbClient_MessageType> clientMessageTypes = new HashSet();
    private boolean inCleanUp = false;
    private boolean isMac;
    private TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter;
    private TS_LibVncDesktopRfb_IRequestString connectionIdRetriever;

    public TS_LibVncDesktopRfbProtocol_Protocol(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfb_IRequestString passwordRetriever, TS_LibVncDesktopRfbProtocol_Settings settings) {
        context = new TS_LibVncDesktopRfbProtocol_Context();
        context.transport = transport;
        this.passwordRetriever = passwordRetriever;
        context.settings = settings;
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.RAW_ENCODING, TS_LibVncDesktopRfbEncoding_DecoderRaw.getInstance());
    }

    public void handshake() throws TS_LibVncDesktopException_UnsupportedProtocolVersion, TS_LibVncDesktopException_UnsupportedSecurityType,
            TS_LibVncDesktopException_AuthenticationFailed, TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal, Throwable {
        logger.info("Starting handshake...");
        var await = TS_ThreadAsyncAwait.runUntil(context.transport.killTrigger.newChild(d.className), Duration.ofSeconds(15), kt -> {
            TGS_FuncMTCUtils.run(() -> {
                context.transport = new TS_LibVncDesktopRfb_HandlerHandshaker(this).handshake(getTransport());
            });
        });
        if (await.exceptionIfFailed().isPresent()) {
            TGS_FuncMTUUtils.thrw(await.exceptionIfFailed().get());
            return;
        }
        logger.info("context.transport:%s".formatted(context.transport.toString()));
        messageQueue = new TS_LibVncDesktopRfbProtocol_MessageQueue(); // TODO Why here?
        logger.info("messageQueue:%s".formatted(messageQueue.toString()));
    }

    public TS_LibVncDesktopRfb_IRequestString getPasswordRetriever() {
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
    public void startNormalHandling(TS_ThreadSyncTrigger killTrigger, TS_LibVncDesktopRfb_ISessionListener rfbSessionListener,
            TS_LibVncDesktopRfb_IRepaintController repaintController, TS_LibVncDesktopRfb_ClipboardController clipboardController) {
        this.rfbSessionListener = rfbSessionListener;
        this.repaintController = repaintController;
//		if (settings.getColorDepth() == 0) {
//			settings.setColorDepth(pixelFormat.depth); // the same the server sent when not initialized yet
//		}
        correctServerPixelFormat();
        context.setPixelFormat(createPixelFormat(context.settings));
        sendMessage(new TS_LibVncDesktopRfbClient_SetPixelFormatMessage(context.pixelFormat));
        logger.fine("sent: %s".formatted(context.pixelFormat.toString()));

        sendSupportedEncodingsMessage(context.settings);
        context.settings.addListener(TS_LibVncDesktopRfbProtocol_Protocol.this); // to support pixel format (color depth), and encodings changes
        context.settings.addListener(repaintController);

        sendRefreshMessage();
        senderTask = new TS_LibVncDesktopRfbProtocol_SenderTask(killTrigger, messageQueue, context.transport, TS_LibVncDesktopRfbProtocol_Protocol.this);
        senderThread = TS_ThreadAsyncRun.now(killTrigger.newChild(d.className), kt -> senderTask.run());
        resetDecoders();
        receiverTask = new TS_LibVncDesktopRfbProtocol_ReceiverTask(killTrigger,
                context.transport, repaintController,
                clipboardController,
                TS_LibVncDesktopRfbProtocol_Protocol.this, baudrateMeter);
        receiverThread = TS_ThreadAsyncRun.now(killTrigger.newChild(d.className), kt -> receiverTask.run());
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
                serverPixelFormat = TS_LibVncDesktopRfbEncoding_PixelFormat.create3bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 6) {
                serverPixelFormat = TS_LibVncDesktopRfbEncoding_PixelFormat.create6bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 8) {
                serverPixelFormat = TS_LibVncDesktopRfbEncoding_PixelFormat.create8bitColorDepthBGRPixelFormat(serverPixelFormat.bigEndianFlag);
            } else if (depth <= 16) {
                serverPixelFormat = TS_LibVncDesktopRfbEncoding_PixelFormat.create16bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
            } else {
                serverPixelFormat = TS_LibVncDesktopRfbEncoding_PixelFormat.create24bitColorDepthPixelFormat(serverPixelFormat.bigEndianFlag);
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

    public void sendMessage(TS_LibVncDesktopRfbClient_ClientToServerMessage message) {
        messageQueue.put(message);
    }

    public void sendSupportedEncodingsMessage(TS_LibVncDesktopRfbProtocol_Settings settings) {
        final LinkedHashSet<TS_LibVncDesktopRfbEncoding_Type> encodings = new LinkedHashSet();
        final var preferredEncoding = settings.getPreferredEncoding();
        if (preferredEncoding != TS_LibVncDesktopRfbEncoding_Type.RAW_ENCODING) {
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
                                encodings.add(TS_LibVncDesktopRfbEncoding_Type.byId(TS_LibVncDesktopRfbEncoding_Type.COMPRESS_LEVEL_0.getId() + compressionLevel));
                            }
                        }
                        case JPEG_QUALITY_LEVEL_0 -> {
                            final var jpegQuality = settings.getJpegQuality();
                            final var colorDepth = settings.getColorDepth();
                            if (jpegQuality > 0 && jpegQuality < 10
                                    && (colorDepth == TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_24
                                    || colorDepth == TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_SERVER_SETTINGS)) {
                                encodings.add(TS_LibVncDesktopRfbEncoding_Type.byId(TS_LibVncDesktopRfbEncoding_Type.JPEG_QUALITY_LEVEL_0.getId() + jpegQuality));
                            }
                        }
                        case COPY_RECT -> {
                            if (settings.isAllowCopyRect()) {
                                encodings.add(TS_LibVncDesktopRfbEncoding_Type.COPY_RECT);
                            }
                        }
                        case RICH_CURSOR -> {
                            if (settings.getMouseCursorTrack() == TS_LibVncDesktopRfbProtocol_LocalPointer.HIDE
                                    || settings.getMouseCursorTrack() == TS_LibVncDesktopRfbProtocol_LocalPointer.ON) {
                                encodings.add(TS_LibVncDesktopRfbEncoding_Type.RICH_CURSOR);
                            }
                        }
                        case CURSOR_POS -> {
                            if (settings.getMouseCursorTrack() == TS_LibVncDesktopRfbProtocol_LocalPointer.HIDE
                                    || settings.getMouseCursorTrack() == TS_LibVncDesktopRfbProtocol_LocalPointer.ON) {
                                encodings.add(TS_LibVncDesktopRfbEncoding_Type.CURSOR_POS);
                            }
                        }
                        default ->
                            encodings.add(e);
                    }
                });
        var encodingsMessage = new TS_LibVncDesktopRfbClient_SetEncodingsMessage(encodings);
        sendMessage(encodingsMessage);
        logger.fine("sent: %s".formatted(encodingsMessage.toString()));
    }

    /**
     * create pixel format by bpp
     */
    private TS_LibVncDesktopRfbEncoding_PixelFormat createPixelFormat(TS_LibVncDesktopRfbProtocol_Settings settings) {
        var serverBigEndianFlag = serverPixelFormat.bigEndianFlag;
        return switch (settings.getColorDepth()) {
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_24 ->
                TS_LibVncDesktopRfbEncoding_PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_16 ->
                TS_LibVncDesktopRfbEncoding_PixelFormat.create16bitColorDepthPixelFormat(serverBigEndianFlag);
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_8 ->
                hackForMacOsXScreenSharingServer(TS_LibVncDesktopRfbEncoding_PixelFormat.create8bitColorDepthBGRPixelFormat(serverBigEndianFlag));
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_6 ->
                hackForMacOsXScreenSharingServer(TS_LibVncDesktopRfbEncoding_PixelFormat.create6bitColorDepthPixelFormat(serverBigEndianFlag));
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_3 ->
                hackForMacOsXScreenSharingServer(TS_LibVncDesktopRfbEncoding_PixelFormat.create3bitColorDepthPixelFormat(serverBigEndianFlag));
            case TS_LibVncDesktopRfbProtocol_Settings.COLOR_DEPTH_SERVER_SETTINGS ->
                serverPixelFormat;
            default ->
                TS_LibVncDesktopRfbEncoding_PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
        }; // unsupported bpp, use default
    }

    private TS_LibVncDesktopRfbEncoding_PixelFormat hackForMacOsXScreenSharingServer(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        if (isMac) {
            pixelFormat.bitsPerPixel = pixelFormat.depth = 16;
        }
        return pixelFormat;
    }

    @Override
    public void settingsChanged(TS_LibVncDesktopCore_SettingsChangedEvent e) {
        var settings = (TS_LibVncDesktopRfbProtocol_Settings) e.getSource();
        if (settings.isChangedEncodings()) {
            sendSupportedEncodingsMessage(settings);
        }
        if (settings.isChangedColorDepth() && receiverTask != null) {
            receiverTask.queueUpdatePixelFormat(createPixelFormat(settings));
        }
    }

    public void sendRefreshMessage() {
        sendMessage(new TS_LibVncDesktopRfbClient_FramebufferUpdateRequestMessage(0, 0, context.fbWidth, context.fbHeight, false));
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
        TS_LibVncDesktopRfbEncoding_ByteBuffer.removeInstance();
    }

    public void setServerPixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat serverPixelFormat) {
        this.serverPixelFormat = serverPixelFormat;
    }

    public TS_LibVncDesktopRfbProtocol_Settings getSettings() {
        return context.getSettings();
    }

    public TS_LibVncDesktopTransport_Transport getTransport() {
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

    public TS_LibVncDesktopRfbEncoding_PixelFormat getPixelFormat() {
        return context.getPixelFormat();
    }

    public void setPixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
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

    public void setProtocolVersion(TS_LibVncDesktopRfb_HandlerHandshaker.ProtocolVersion protocolVersion) {
        context.setProtocolVersion(protocolVersion);
    }

    public TS_LibVncDesktopRfb_HandlerHandshaker.ProtocolVersion getProtocolVersion() {
        return context.getProtocolVersion();
    }

    public void registerRfbEncodings() {
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.TIGHT, new TS_LibVncDesktopRfbEncoding_DecoderTight());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.HEXTILE, new TS_LibVncDesktopRfbEncoding_DecoderHextile());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.ZRLE, new TS_LibVncDesktopRfbEncoding_DecoderZRLE());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.ZLIB, new TS_LibVncDesktopRfbEncoding_DecoderZlib());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.RRE, new TS_LibVncDesktopRfbEncoding_DecoderRRE());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.COPY_RECT, new TS_LibVncDesktopRfbEncoding_DecoderCopyRect());

        decoders.put(TS_LibVncDesktopRfbEncoding_Type.RICH_CURSOR, new TS_LibVncDesktopRfbEncoding_DecoderRichCursor());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.DESKTOP_SIZE, new TS_LibVncDesktopRfbEncoding_DecoderDesktopSize());
        decoders.put(TS_LibVncDesktopRfbEncoding_Type.CURSOR_POS, new TS_LibVncDesktopRfbEncoding_DecoderCursorPos());
    }

    public void resetDecoders() {
        decoders.values().stream()
                .filter(decoder -> (decoder != null))
                .forEachOrdered(decoder -> {
                    decoder.reset();
                });
    }

    public TS_LibVncDesktopRfbEncoding_Decoder getDecoderByType(TS_LibVncDesktopRfbEncoding_Type type) {
        return decoders.get(type);
    }

    public void registerEncoding(TS_LibVncDesktopRfb_CapabilityInfo capInfo) {
        try {
            final var encodingType = TS_LibVncDesktopRfbEncoding_Type.byId(capInfo.getCode());
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

    public void registerClientMessageType(TS_LibVncDesktopRfb_CapabilityInfo capInfo) {
        try {
            final var clientMessageType = TS_LibVncDesktopRfbClient_MessageType.byId(capInfo.getCode());
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
    public boolean isSupported(TS_LibVncDesktopRfbClient_MessageType type) {
        return clientMessageTypes.contains(type) || TS_LibVncDesktopRfbClient_MessageType.isStandardType(type);
    }

    public void setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType tunnelType) {
        context.setTunnelType(tunnelType);
    }

    public TS_LibVncDesktopRfbProtocol_TunnelType getTunnelType() {
        return context.getTunnelType();
    }

    public void setMac(boolean isMac) {
        this.isMac = isMac;
    }

    public void setBaudrateMeter(TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter) {
        this.baudrateMeter = baudrateMeter;
    }

    public int kBPS() {
        return baudrateMeter == null ? -1 : baudrateMeter.kBPS();
    }

    public boolean isMac() {
        return isMac;
    }

    public void setConnectionIdRetriever(TS_LibVncDesktopRfb_IRequestString connectionIdRetriever) {
        this.connectionIdRetriever = connectionIdRetriever;
    }

    public TS_LibVncDesktopRfb_IRequestString getConnectionIdRetriever() {
        return connectionIdRetriever;
    }
}
