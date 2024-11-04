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
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbClient_ClientToServerMessage;

public class TS_LibVncDesktopRfbProtocol_SenderTask implements Runnable {

    private final TS_LibVncDesktopRfbProtocol_MessageQueue queue;
    private final TS_LibVncDesktopTransport_Transport transport;
    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private final TS_ThreadSyncTrigger killTrigger;

    /**
     * Create sender task Task runs as thread, receive messages from queue and
     * sends them to transport. When no messages appears in queue longer than
     * timeout period, sends FramebufferUpdate request
     *
     * @param messageQueue queue to poll messages
     * @param transport transport to send messages out
     * @param protocol session lifecircle support
     */
    public TS_LibVncDesktopRfbProtocol_SenderTask(TS_ThreadSyncTrigger killTrigger, TS_LibVncDesktopRfbProtocol_MessageQueue messageQueue, TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol) {
        this.killTrigger = killTrigger;
        this.queue = messageQueue;
        this.transport = transport;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        TS_LibVncDesktopRfbClient_ClientToServerMessage message;
        try {
            while (killTrigger.hasNotTriggered() && !Thread.currentThread().isInterrupted()) {
                message = queue.get();
                if (message != null) {
                    message.send(transport);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TS_LibVncDesktopException_Transport e) {
            Logger.getLogger(getClass().getName()).severe("Close session: %s".formatted(e.getMessage()));
            protocol.cleanUpSession("Connection closed");
        } catch (Throwable te) {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            te.printStackTrace(pw);
            protocol.cleanUpSession(te.getMessage() + "\n" + sw.toString());
        }
        Logger.getLogger(getClass().getName()).finer("Sender task stopped");
    }

}
