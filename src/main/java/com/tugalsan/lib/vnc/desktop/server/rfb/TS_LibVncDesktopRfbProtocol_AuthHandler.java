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

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_AuthenticationFailed;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedSecurityType;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Fatal;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_ClosedConnection;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_ServerInitMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;

import java.util.logging.Logger;

public abstract class TS_LibVncDesktopRfbProtocol_AuthHandler {

    private static final int AUTH_RESULT_OK = 0;
//	private static final int AUTH_RESULT_FAILED = 1;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopRfbProtocol_AuthHandler.class.getName());
    
    /**
     * Not thread safe, no need to be thread safe
     */
    protected Logger logger() {
        return logger;
    }

    /**
     * Authenticate using appropriate auth scheme
     *
     * @param transport transport for i/o
     * @param protocol rfb protocol object
     * @return transport for future i/o using
     */
    public abstract TS_LibVncDesktopTransport_Transport authenticate(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol)
            throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal, TS_LibVncDesktopException_UnsupportedSecurityType;

    public abstract TS_LibVncDesktopRfbProtocol_AuthTypes getType();

    public int getId() {
        return getType().getId();
    }

    public String getName() {
        return getType().name();
    }

    /**
     * Check Security Result received from server May be: * 0 - OK * 1 - Failed
     *
     * Do not check on NoneAuthentication
     */
    public void checkSecurityResult(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport,
            TS_LibVncDesktopException_AuthenticationFailed {
        final var securityResult = transport.readInt32();
        logger().fine("Security result: %d%s".formatted(securityResult , (AUTH_RESULT_OK == securityResult ? " (OK)" : " (Failed)")));
        if (securityResult != AUTH_RESULT_OK) {
            try {
                var reason = transport.readString();
                logger().fine("Security result reason: %s".formatted(reason));
                throw new TS_LibVncDesktopException_AuthenticationFailed(reason);
            } catch (TS_LibVncDesktopException_ClosedConnection e) {
                // protocol version 3.3 and 3.7 does not send reason string,
                // but silently closes the connection
                throw new TS_LibVncDesktopException_AuthenticationFailed("Authentication failed");
            }
        }
    }

    public void initProcedure(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol) throws TS_LibVncDesktopException_Transport {
        sendClientInitMessage(transport, protocol.getSettings().getSharedFlag());
        var serverInitMessage = readServerInitMessage(transport);
        completeContextData(serverInitMessage, protocol);
        protocol.registerRfbEncodings();
    }

    protected TS_LibVncDesktopRfbEncoding_ServerInitMessage readServerInitMessage(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        var serverInitMessage = new TS_LibVncDesktopRfbEncoding_ServerInitMessage().readFrom(transport);
        logger().fine("Read: %s".formatted(serverInitMessage.toString()));
        return serverInitMessage;
    }

    protected void sendClientInitMessage(TS_LibVncDesktopTransport_Transport transport, byte sharedFlag) throws TS_LibVncDesktopException_Transport {
        logger().fine("Sent client-init-message: %s".formatted(String.valueOf(sharedFlag)));
        transport.writeByte(sharedFlag).flush();
    }

    protected void completeContextData(TS_LibVncDesktopRfbEncoding_ServerInitMessage serverInitMessage, TS_LibVncDesktopRfbProtocol_Protocol protocol) {
        protocol.setServerPixelFormat(serverInitMessage.getPixelFormat());
        protocol.setFbWidth(serverInitMessage.getFramebufferWidth());
        protocol.setFbHeight(serverInitMessage.getFramebufferHeight());
        protocol.setRemoteDesktopName(serverInitMessage.getName());
    }
}
