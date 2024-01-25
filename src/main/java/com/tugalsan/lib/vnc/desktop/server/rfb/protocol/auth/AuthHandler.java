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
package com.tugalsan.lib.vnc.desktop.server.rfb.protocol.auth;

import com.tugalsan.lib.vnc.desktop.server.exceptions.AuthenticationFailedException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.UnsupportedSecurityTypeException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.FatalException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.ClosedConnectionException;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.ServerInitMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.Protocol;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

import java.util.logging.Logger;

public abstract class AuthHandler {

    private static final int AUTH_RESULT_OK = 0;
//	private static final int AUTH_RESULT_FAILED = 1;
    private static final Logger logger = Logger.getLogger(AuthHandler.class.getName());
    
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
    public abstract Transport authenticate(Transport transport, Protocol protocol)
            throws TransportException, FatalException, UnsupportedSecurityTypeException;

    public abstract SecurityType getType();

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
    public void checkSecurityResult(Transport transport) throws TransportException,
            AuthenticationFailedException {
        final var securityResult = transport.readInt32();
        logger().fine("Security result: %d%s".formatted(securityResult , (AUTH_RESULT_OK == securityResult ? " (OK)" : " (Failed)")));
        if (securityResult != AUTH_RESULT_OK) {
            try {
                var reason = transport.readString();
                logger().fine("Security result reason: %s".formatted(reason));
                throw new AuthenticationFailedException(reason);
            } catch (ClosedConnectionException e) {
                // protocol version 3.3 and 3.7 does not send reason string,
                // but silently closes the connection
                throw new AuthenticationFailedException("Authentication failed");
            }
        }
    }

    public void initProcedure(Transport transport, Protocol protocol) throws TransportException {
        sendClientInitMessage(transport, protocol.getSettings().getSharedFlag());
        var serverInitMessage = readServerInitMessage(transport);
        completeContextData(serverInitMessage, protocol);
        protocol.registerRfbEncodings();
    }

    protected ServerInitMessage readServerInitMessage(Transport transport) throws TransportException {
        var serverInitMessage = new ServerInitMessage().readFrom(transport);
        logger().fine("Read: %s".formatted(serverInitMessage.toString()));
        return serverInitMessage;
    }

    protected void sendClientInitMessage(Transport transport, byte sharedFlag) throws TransportException {
        logger().fine("Sent client-init-message: %s".formatted(String.valueOf(sharedFlag)));
        transport.writeByte(sharedFlag).flush();
    }

    protected void completeContextData(ServerInitMessage serverInitMessage, Protocol protocol) {
        protocol.setServerPixelFormat(serverInitMessage.getPixelFormat());
        protocol.setFbWidth(serverInitMessage.getFramebufferWidth());
        protocol.setFbHeight(serverInitMessage.getFramebufferHeight());
        protocol.setRemoteDesktopName(serverInitMessage.getName());
    }
}
