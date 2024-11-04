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

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Fatal;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedSecurityType;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_CapabilityInfo;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopUtils_Strings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TS_LibVncDesktopRfbProtocol_AuthTypeTight extends TS_LibVncDesktopRfbProtocol_AuthHandler {

    private final Map<Integer, TS_LibVncDesktopRfbProtocol_AuthHandler> registeredAuthHandlers = new HashMap();
    private final Map<Integer, TS_LibVncDesktopRfbProtocol_TunnelHandler> registeredTunnelHandlers = new HashMap();

    public TS_LibVncDesktopRfbProtocol_AuthTypeTight() {
    }

    public void registerTunnelingHandler(TS_LibVncDesktopRfbProtocol_TunnelHandler handler) {
        registeredTunnelHandlers.put(handler.getId(), handler);
    }

    public void registerAuthHandler(TS_LibVncDesktopRfbProtocol_AuthHandler handler) {
        registeredAuthHandlers.put(handler.getId(), handler);
    }

    @Override
    public TS_LibVncDesktopRfbProtocol_AuthTypes getType() {
        return TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION;
    }

    @Override
    public TS_LibVncDesktopTransport_Transport authenticate(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol)
            throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal, TS_LibVncDesktopException_UnsupportedSecurityType {
        transport = tunnelingNegotiation(transport, protocol);
        authorizationNegotiation(transport, protocol);
        protocol.setTight(true);
        return transport;
    }

    @Override
    public void initProcedure(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol) throws TS_LibVncDesktopException_Transport {
        capabilitiesNegotiation(transport, protocol);
        protocol.registerRfbEncodings();
    }

    /**
     * Capabilities negotiation consists of server-to-client message, where
     * server introduces its capabilities, and client-to-server message, which
     * introduces only those client capabilities which are supported by server
     * and encodings supported by server.
     *
     * This data immediately follows the server initialisation message.
     *
     * typedef struct _rfbInteractionCapsMsg { CARD16 nServerMessageTypes;
     * CARD16 nClientMessageTypes; CARD16 nEncodingTypes; CARD16
     * pad;><------><------>// reserved, must be 0 // followed by
     * nServerMessageTypes * rfbCapabilityInfo structures // followed by
     * nClientMessageTypes * rfbCapabilityInfo structures }
     * rfbInteractionCapsMsg; #define sz_rfbInteractionCapsMsg 8
     *
     * nServerMessageTypes | UINT16 | Number of server message types server
     * announces. nClientMessageTypes | UINT16 | Number of client message types
     * server announces. nEncodingTypes | UINT16 | Number of encoding types
     * server announces. ServerMessageTypes | RFBCAPABILITY x
     * nServerMessageTypes | Server side messages which server supports.
     * ClientMessageTypes | RFBCAPABILITY x nClientMessageTypes | Client side
     * messages which server supports. Encodings | RFBCAPABILITY x
     * nEncodingTypes | Encoding types which server supports.
     *
     * Client replies with message in exactly the same format, listing only
     * those capabilities which are supported both by server and the client.
     * Once all three initialization stages are successfully finished, client
     * and server switch to normal protocol flow.
     *
     */
    void capabilitiesNegotiation(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol) throws TS_LibVncDesktopException_Transport {
        sendClientInitMessage(transport, protocol.getSettings().getSharedFlag());
        var serverInitMessage = readServerInitMessage(transport);

        var nServerMessageTypes = transport.readUInt16();
        var nClientMessageTypes = transport.readUInt16();
        var nEncodingTypes = transport.readUInt16();
        transport.readUInt16(); //padding

        logger().fine("nServerMessageTypes: %d, nClientMessageTypes: %d, nEncodingTypes: %d".formatted(nServerMessageTypes, nClientMessageTypes, nEncodingTypes));

        registerServerMessagesTypes(transport, protocol, nServerMessageTypes);
        registerClientMessagesTypes(transport, protocol, nClientMessageTypes);
        registerEncodings(transport, protocol, nEncodingTypes);
        completeContextData(serverInitMessage, protocol);
    }

    private void registerServerMessagesTypes(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol, int count) throws TS_LibVncDesktopException_Transport {
        while (count-- > 0) {
            var capInfoReceived = new TS_LibVncDesktopRfb_CapabilityInfo().readFrom(transport);
            logger().fine("Server message type: %s".formatted(capInfoReceived.toString()));
        }
    }

    private void registerClientMessagesTypes(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol, int count) throws TS_LibVncDesktopException_Transport {
        while (count-- > 0) {
            var capInfoReceived = new TS_LibVncDesktopRfb_CapabilityInfo().readFrom(transport);
            logger().fine("Client message type: %s".formatted(capInfoReceived.toString()));
            protocol.registerClientMessageType(capInfoReceived);
        }
    }

    private void registerEncodings(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol, int count) throws TS_LibVncDesktopException_Transport {
        while (count-- > 0) {
            var capInfoReceived = new TS_LibVncDesktopRfb_CapabilityInfo().readFrom(transport);
            logger().fine("Encoding: %s".formatted(capInfoReceived.toString()));
            protocol.registerEncoding(capInfoReceived);
        }
    }

    /**
     * Negotiation of Tunneling Capabilities (protocol versions 3.7t, 3.8t)
     *
     * If the chosen security type is rfbSecTypeTight, the server sends a list
     * of supported tunneling methods ("tunneling" refers to any additional
     * layer of data transformation, such as encryption or external
     * compression.)
     *
     * nTunnelTypes specifies the number of following rfbCapabilityInfo
     * structures that list all supported tunneling methods in the order of
     * preference.
     *
     * NOTE: If nTunnelTypes is 0, that tells the client that no tunneling can
     * be used, and the client should not send a response requesting a tunneling
     * method.
     *
     * typedef struct _rfbTunnelingCapsMsg { CARD32 nTunnelTypes; //followed by
     * nTunnelTypes * rfbCapabilityInfo structures } rfbTunnelingCapsMsg;
     * #define sz_rfbTunnelingCapsMsg 4
     * ----------------------------------------------------------------------------
     * Tunneling Method Request (protocol versions 3.7t, 3.8t)
     *
     * If the list of tunneling capabilities sent by the server was not empty,
     * the client should reply with a 32-bit code specifying a particular
     * tunneling method. The following code should be used for no tunneling.
     *
     * #define rfbNoTunneling 0 #define sig_rfbNoTunneling "NOTUNNEL"
     */
    TS_LibVncDesktopTransport_Transport tunnelingNegotiation(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol)
            throws TS_LibVncDesktopException_Transport {
        var newTransport = transport;
        int tunnelsCount;
        tunnelsCount = (int) transport.readUInt32();
        logger().fine("Tunneling capabilities: %d".formatted(tunnelsCount));
        var tunnelCodes = new int[tunnelsCount];
        if (tunnelsCount > 0) {
            for (var i = 0; i < tunnelsCount; ++i) {
                var rfbCapabilityInfo = new TS_LibVncDesktopRfb_CapabilityInfo().readFrom(transport);
                tunnelCodes[i] = rfbCapabilityInfo.getCode();
                logger().fine(rfbCapabilityInfo.toString());
            }
            int selectedTunnelCode;
            if (tunnelsCount > 0) {
                for (var i = 0; i < tunnelsCount; ++i) {
                    final var tunnelHandler = registeredTunnelHandlers.get(tunnelCodes[i]);
                    if (tunnelHandler != null) {
                        selectedTunnelCode = tunnelCodes[i];
                        transport.writeInt32(selectedTunnelCode).flush();
                        logger().fine("Accepted tunneling type: %d".formatted(selectedTunnelCode));
                        newTransport = tunnelHandler.createTunnel(transport);
                        logger().fine("Tunnel created: %s".formatted(TS_LibVncDesktopRfbProtocol_TunnelType.byCode(selectedTunnelCode).toString()));
                        protocol.setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType.byCode(selectedTunnelCode));
                        break;
                    }
                }
            }
        }
        if (protocol.getTunnelType() == null) {
            protocol.setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL);
            if (tunnelsCount > 0) {
                transport.writeInt32(TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL.code).flush();
            }
            logger().fine("Accepted tunneling type: %s".formatted(TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL.toString()));
        }
        return newTransport;
    }

    /**
     * Negotiation of Authentication Capabilities (protocol versions 3.7t, 3.8t)
     *
     * After setting up tunneling, the server sends a list of supported
     * authentication schemes.
     *
     * nAuthTypes specifies the number of following rfbCapabilityInfo structures
     * that list all supported authentication schemes in the order of
     * preference.
     *
     * NOTE: If nAuthTypes is 0, that tells the client that no authentication is
     * necessary, and the client should not send a response requesting an
     * authentication scheme.
     *
     * typedef struct _rfbAuthenticationCapsMsg { CARD32 nAuthTypes; // followed
     * by nAuthTypes * rfbCapabilityInfo structures } rfbAuthenticationCapsMsg;
     * #define sz_rfbAuthenticationCapsMsg 4
     *
     */
    void authorizationNegotiation(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol)
            throws TS_LibVncDesktopException_UnsupportedSecurityType, TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal {
        var authCount = transport.readInt32();
        logger().fine("Auth capabilities: %d".formatted(authCount));
        var cap = new byte[authCount];
        for (var i = 0; i < authCount; ++i) {
            var rfbCapabilityInfo = new TS_LibVncDesktopRfb_CapabilityInfo().readFrom(transport);
            cap[i] = (byte) rfbCapabilityInfo.getCode();
            logger().fine(rfbCapabilityInfo.toString());
        }
        TS_LibVncDesktopRfbProtocol_AuthHandler authHandler = null;
        if (authCount > 0) {
            for (var i = 0; i < authCount; ++i) {
                authHandler = registeredAuthHandlers.get((int) cap[i]);
                if (authHandler != null) {
                    //sending back RFB capability code
                    transport.writeInt32(authHandler.getId()).flush();
                    break;
                }
            }
        } else {
            authHandler = registeredAuthHandlers.get(TS_LibVncDesktopRfbProtocol_AuthTypes.NONE_AUTHENTICATION.getId());
        }
        if (null == authHandler) {
            throw new TS_LibVncDesktopException_UnsupportedSecurityType("Server auth types: " + TS_LibVncDesktopUtils_Strings.toString(cap)
                    + ", supported auth types: " + registeredAuthHandlers.values());
        }
        logger().fine("Auth capability accepted: %s".formatted(authHandler.getName()));
        authHandler.authenticate(transport, protocol);
    }

}
