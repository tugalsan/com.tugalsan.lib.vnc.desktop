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

import module com.tugalsan.api.thread;
import module java.logging;
import com.tugalsan.lib.vnc.desktop.server.exceptions.*;
import com.tugalsan.lib.vnc.desktop.server.base.*;
import java.io.*;

public class TS_LibVncDesktopRfbProtocol_ReceiverTask implements Runnable {

    private static final byte FRAMEBUFFER_UPDATE = 0;
    private static final byte SET_COLOR_MAP_ENTRIES = 1;
    private static final byte BELL = 2;
    private static final byte SERVER_CUT_TEXT = 3;

    private static final Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol.ReceiverTask");
    private final TS_LibVncDesktopTransport_Transport transport;
    private final TS_ThreadSyncTrigger killTrigger;
    private TS_LibVncDesktopDrawing_Renderer renderer;
    private final TS_LibVncDesktopRfb_IRepaintController repaintController;
    private final TS_LibVncDesktopRfb_ClipboardController clipboardController;
    protected TS_LibVncDesktopRfbClient_FramebufferUpdateRequestMessage fullscreenFbUpdateIncrementalRequest;
    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private final TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter;
    private TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat;
    private volatile boolean needSendPixelFormat;

    public TS_LibVncDesktopRfbProtocol_ReceiverTask(TS_ThreadSyncTrigger killTrigger, TS_LibVncDesktopTransport_Transport transport,
            TS_LibVncDesktopRfb_IRepaintController repaintController, TS_LibVncDesktopRfb_ClipboardController clipboardController,
            TS_LibVncDesktopRfbProtocol_Protocol protocol, TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter) {
        this.killTrigger = killTrigger;
        this.transport = transport;
        this.repaintController = repaintController;
        this.clipboardController = clipboardController;
        this.protocol = protocol;
        this.baudrateMeter = baudrateMeter;
        renderer = repaintController.createRenderer(transport, protocol.getFbWidth(), protocol.getFbHeight(),
                protocol.getPixelFormat());
        fullscreenFbUpdateIncrementalRequest
                = new TS_LibVncDesktopRfbClient_FramebufferUpdateRequestMessage(0, 0, protocol.getFbWidth(), protocol.getFbHeight(), true);
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
        } catch (TS_LibVncDesktopException_Transport e) {
            logger.severe("Close session: %s".formatted(e.getMessage()));
            protocol.cleanUpSession("Connection closed.");
        } catch (TS_LibVncDesktopException_Protocol e) {
            logger.severe(e.getMessage());
            protocol.cleanUpSession(e.getMessage() + "\nConnection closed.");
        } catch (TS_LibVncDesktopException_Common e) {
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

    private void setColorMapEntries() throws TS_LibVncDesktopException_Transport {
        transport.readByte();  // padding
        transport.readUInt16(); // first color index
        var length = transport.readUInt16();
        while (length-- > 0) {
            transport.readUInt16(); // R
            transport.readUInt16(); // G
            transport.readUInt16(); // B
        }
    }

    private void serverCutText() throws TS_LibVncDesktopException_Transport {
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

    public void framebufferUpdateMessage() throws TS_LibVncDesktopException_Common {
        transport.skip(1); // padding
        var numberOfRectangles = transport.readUInt16();
        while (numberOfRectangles-- > 0) {
            var rect = new TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle();
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
                                    = new TS_LibVncDesktopRfbClient_FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, true);
                        }
                        renderer = repaintController.createRenderer(transport, rect.width, rect.height,
                                protocol.getPixelFormat());
                        protocol.sendMessage(new TS_LibVncDesktopRfbClient_FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, false));
                        return;
                    }
                    default -> repaintController.repaintBitmap(rect);
                }
            } else {
                throw new TS_LibVncDesktopException_Common("Unprocessed encoding: " + rect.toString());
            }
        }
        if (needSendPixelFormat) {
            synchronized (this) {
                if (needSendPixelFormat) {
                    needSendPixelFormat = false;
                    protocol.setPixelFormat(pixelFormat);
                    protocol.sendMessage(new TS_LibVncDesktopRfbClient_SetPixelFormatMessage(pixelFormat));
                    logger.fine("sent: %s" .formatted(pixelFormat.toString()));
                    protocol.sendRefreshMessage();
                    logger.fine("sent: nonincremental fb update");
                }
            }
        } else {
            protocol.sendMessage(fullscreenFbUpdateIncrementalRequest);
        }
    }

    public synchronized void queueUpdatePixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat pf) {
        pixelFormat = pf;
        needSendPixelFormat = true;
//		protocol.sendMessage(new FramebufferUpdateRequestMessage(0, 0, 1, 1, false));
    }

}
