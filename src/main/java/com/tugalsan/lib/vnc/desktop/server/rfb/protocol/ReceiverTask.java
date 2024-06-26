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
import com.tugalsan.lib.vnc.desktop.server.drawing.Renderer;
import com.tugalsan.lib.vnc.desktop.server.exceptions.CommonException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.ProtocolException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.rfb.ClipboardController;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRepaintController;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.FramebufferUpdateRequestMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.SetPixelFormatMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.PixelFormat;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.tugalsan.lib.vnc.desktop.server.transport.BaudrateMeter;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class ReceiverTask implements Runnable {

    private static final byte FRAMEBUFFER_UPDATE = 0;
    private static final byte SET_COLOR_MAP_ENTRIES = 1;
    private static final byte BELL = 2;
    private static final byte SERVER_CUT_TEXT = 3;

    private static final Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol.ReceiverTask");
    private final Transport transport;
    private final TS_ThreadSyncTrigger killTrigger;
    private Renderer renderer;
    private final IRepaintController repaintController;
    private final ClipboardController clipboardController;
    protected FramebufferUpdateRequestMessage fullscreenFbUpdateIncrementalRequest;
    private final Protocol protocol;
    private final BaudrateMeter baudrateMeter;
    private PixelFormat pixelFormat;
    private volatile boolean needSendPixelFormat;

    public ReceiverTask(TS_ThreadSyncTrigger killTrigger, Transport transport,
            IRepaintController repaintController, ClipboardController clipboardController,
            Protocol protocol, BaudrateMeter baudrateMeter) {
        this.killTrigger = killTrigger;
        this.transport = transport;
        this.repaintController = repaintController;
        this.clipboardController = clipboardController;
        this.protocol = protocol;
        this.baudrateMeter = baudrateMeter;
        renderer = repaintController.createRenderer(transport, protocol.getFbWidth(), protocol.getFbHeight(),
                protocol.getPixelFormat());
        fullscreenFbUpdateIncrementalRequest
                = new FramebufferUpdateRequestMessage(0, 0, protocol.getFbWidth(), protocol.getFbHeight(), true);
    }

    @Override
    public void run() {
        try {
            while (killTrigger.hasNotTriggered() && !Thread.currentThread().isInterrupted()) {
                var messageId = transport.readByte();
                switch (messageId) {
                    case FRAMEBUFFER_UPDATE -> //					logger.fine("Server message: FramebufferUpdate (0)");
                        framebufferUpdateMessage();
                    case SET_COLOR_MAP_ENTRIES -> {
                        logger.severe("Server message SetColorMapEntries is not implemented. Skip.");
                        setColorMapEntries();
                    }
                    case BELL -> {
                        logger.fine("Server message: Bell");
                        System.out.print("\0007");
                        System.out.flush();
                    }
                    case SERVER_CUT_TEXT -> {
                        logger.fine("Server message: CutText (3)");
                        serverCutText();
                    }
                    default ->
                        logger.severe("Unsupported server message. Id = %s".formatted(String.valueOf(messageId)));
                }
            }
        } catch (TransportException e) {
            logger.severe("Close session: %s".formatted(e.getMessage()));
            protocol.cleanUpSession("Connection closed.");
        } catch (ProtocolException e) {
            logger.severe(e.getMessage());
            protocol.cleanUpSession(e.getMessage() + "\nConnection closed.");
        } catch (CommonException e) {
            logger.severe(e.getMessage());
            protocol.cleanUpSession("Connection closed..");
        } catch (Throwable te) {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            te.printStackTrace(pw);
            protocol.cleanUpSession(te.getMessage() + "\n" + sw.toString());
        }
        Logger.getLogger(getClass().getName()).finer("Receiver task stopped");
    }

    private void setColorMapEntries() throws TransportException {
        transport.readByte();  // padding
        transport.readUInt16(); // first color index
        var length = transport.readUInt16();
        while (length-- > 0) {
            transport.readUInt16(); // R
            transport.readUInt16(); // G
            transport.readUInt16(); // B
        }
    }

    private void serverCutText() throws TransportException {
        transport.readByte();  // padding
        transport.readInt16(); // padding
        long length = transport.readInt32();
        if (0 == length) {
            return;
        }
        if (length > Integer.MAX_VALUE) {
            clipboardController.updateSystemClipboard(transport.readBytes(Integer.MAX_VALUE));
            clipboardController.updateSystemClipboard(transport.readBytes((int) (length - Integer.MAX_VALUE)));
        } else {
            clipboardController.updateSystemClipboard(transport.readBytes((int) length));
        }
    }

    public void framebufferUpdateMessage() throws CommonException {
        transport.skip(1); // padding
        var numberOfRectangles = transport.readUInt16();
        while (numberOfRectangles-- > 0) {
            var rect = new FramebufferUpdateRectangle();
            rect.fill(transport);

            var decoder = protocol.getDecoderByType(rect.getEncodingType());
//			logger.finer(rect.toString() + (0 == numberOfRectangles ? "\n---" : ""));
            if (decoder != null) {
                try {
                    if (baudrateMeter != null) {
                        baudrateMeter.startMeasuringCycle();
                    }
                    decoder.decode(transport, renderer, rect);
                } finally {
                    if (baudrateMeter != null) {
                        baudrateMeter.stopMeasuringCycle();
                    }
                }
                if (null
                        == rect.getEncodingType()) {
                    repaintController.repaintBitmap(rect);
                } else switch (rect.getEncodingType()) {
                    case RICH_CURSOR, CURSOR_POS -> repaintController.repaintCursor();
                    case DESKTOP_SIZE -> {
                        synchronized (this) {
                            fullscreenFbUpdateIncrementalRequest
                                    = new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, true);
                        }
                        renderer = repaintController.createRenderer(transport, rect.width, rect.height,
                                protocol.getPixelFormat());
                        protocol.sendMessage(new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, false));
                        return;
                    }
                    default -> repaintController.repaintBitmap(rect);
                }
            } else {
                throw new CommonException("Unprocessed encoding: " + rect.toString());
            }
        }
        if (needSendPixelFormat) {
            synchronized (this) {
                if (needSendPixelFormat) {
                    needSendPixelFormat = false;
                    protocol.setPixelFormat(pixelFormat);
                    protocol.sendMessage(new SetPixelFormatMessage(pixelFormat));
                    logger.fine("sent: %s" .formatted(pixelFormat.toString()));
                    protocol.sendRefreshMessage();
                    logger.fine("sent: nonincremental fb update");
                }
            }
        } else {
            protocol.sendMessage(fullscreenFbUpdateIncrementalRequest);
        }
    }

    public synchronized void queueUpdatePixelFormat(PixelFormat pf) {
        pixelFormat = pf;
        needSendPixelFormat = true;
//		protocol.sendMessage(new FramebufferUpdateRequestMessage(0, 0, 1, 1, false));
    }

}
