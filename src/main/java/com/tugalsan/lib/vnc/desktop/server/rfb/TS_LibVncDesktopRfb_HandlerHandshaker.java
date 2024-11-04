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
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedProtocolVersion;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_UnsupportedSecurityType;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Fatal;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopUtils_Strings;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author dime at glavsoft.com
 */
public class TS_LibVncDesktopRfb_HandlerHandshaker {

    private static final int PROTOCOL_STRING_LENGTH = 12;
    private static final String RFB_PROTOCOL_STRING_REGEXP = "^RFB (\\d\\d\\d).(\\d\\d\\d)\n$";
    private static final String DISPATCHER_PROTOCOL_STRING = "TCPDISPATCH\n";

    private static final int MIN_SUPPORTED_VERSION_MAJOR = 3;
    private static final int MIN_SUPPORTED_VERSION_MINOR = 3;

    private static final int MAX_SUPPORTED_VERSION_MAJOR = 3;
    private static final int MAX_SUPPORTED_VERSION_MINOR = 8;
    protected static final int DISPATCHER_PROTOCOL_VERSION = 3;
    protected static final int KEEP_ALIVE_BYTE = 0;
    protected static final int START_BYTE = 1;
    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopRfb_HandlerHandshaker.class.getName());
    private final Map<Integer, TS_LibVncDesktopRfbProtocol_AuthHandler> registeredAuthHandlers = new HashMap();

    public TS_LibVncDesktopRfb_HandlerHandshaker(TS_LibVncDesktopRfbProtocol_Protocol protocol) {
        this.protocol = protocol;
        registerAuthHandler(TS_LibVncDesktopRfbProtocol_AuthTypes.NONE_AUTHENTICATION.getId(), new TS_LibVncDesktopRfbProtocol_AuthTypeNone());
        registerAuthHandler(TS_LibVncDesktopRfbProtocol_AuthTypes.VNC_AUTHENTICATION.getId(), new TS_LibVncDesktopRfbProtocol_AuthTypeVnc());

        final var tightAuthentication = new TS_LibVncDesktopRfbProtocol_AuthTypeTight();
        tightAuthentication.registerAuthHandler(new TS_LibVncDesktopRfbProtocol_AuthTypeNone());
        tightAuthentication.registerAuthHandler(new TS_LibVncDesktopRfbProtocol_AuthTypeVnc());
        if (protocol.getSettings().getTunnelType() != TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL
                && TS_LibVncDesktopRfbProtocol_TunnelSsl.isTransportAvailable()) {
            tightAuthentication.registerTunnelingHandler(new TS_LibVncDesktopRfbProtocol_TunnelSsl());
            registerAuthHandler(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT2_AUTHENTICATION.getId(), tightAuthentication);
        }
        registerAuthHandler(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION.getId(), tightAuthentication);
    }

    public TS_LibVncDesktopTransport_Transport handshake(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_UnsupportedProtocolVersion, TS_LibVncDesktopException_AuthenticationFailed, TS_LibVncDesktopException_Fatal, TS_LibVncDesktopException_UnsupportedSecurityType {
        var protocolString = transport.readString(PROTOCOL_STRING_LENGTH);
        if (isDispatcherConnection(protocolString)) {
            handshakeToDispatcher(transport);
            protocolString = transport.readString(PROTOCOL_STRING_LENGTH);
        }
        var ver = matchProtocolVersion(protocolString);
        transport.write(TS_LibVncDesktopUtils_Strings.getBytesWithCharset("RFB 00" + ver.major + ".00" + ver.minor + "\n", TS_LibVncDesktopTransport_Transport.ISO_8859_1)).flush();
        protocol.setProtocolVersion(ver);
        logger.info("Set protocol version to: %s".formatted(ver.toString()));
        transport = auth(transport, ver);
        logger.info("Transport created");
        return transport;
    }

    /**
     * Make dispatcher connection
     *
     * Dispatcher protocol v.3: '<-' means receive from dispatcher, '->' means
     * send to dispatcher <- "TCPDISPATCH\n" &mdash; already received at this
     * point <- UInt8 numSupportedVersions value      <- numSupportedVersions UInt8 values of supported version num
     * -> UInt8 value of version accepted -> UInt8 remoteHostRole value (0 ==
     * RFB Server or 1 == RFB Client/viewer) -> UInt32 connId
     * <- UInt32 connId (when 0 == connId, then dispatcher generates unique random connId value
     * 		and sends it to clients, else it doesn't send the one)
     * -> UInt8 secret keyword string length -> String (byte array of ASCII
     * characters) - secret keyword -> UInt8 dispatcher name string length (may
     * equals to 0) -> String (byte array of ASCII characters) - dispatcher name
     * <- UInt8 dispatcher name string length <- String (byte array of ASCII
     * characters) - dispatcher name <- 0 'keep alive byte' or non zero 'start
     * byte' (1) On keep alive byte immediately answer with the same byte, and
     * wain for next byte, on start byte go to ordinary rfb negotiation.
     *
     * @param transport
     *
     * @throws TS_LibVncDesktopException_Transport when some io error happens
     * @throws TS_LibVncDesktopException_UnsupportedProtocolVersion when protocol doesn't match
     * @throws TS_LibVncDesktopException_AuthenticationFailed when connectionId provided by user
     * is wrong
     */
    private void handshakeToDispatcher(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_UnsupportedProtocolVersion, TS_LibVncDesktopException_AuthenticationFailed {
        var numSupportedVersions = transport.readUInt8(); // receive num of supported version followed (u8)
        List<Integer> remoteVersions = new ArrayList(numSupportedVersions);
        for (var i = 0; i < numSupportedVersions; ++i) {
            remoteVersions.add(transport.readUInt8()); // receive supported protocol versions (numSupportedVersions x u8)
        }
        logger.fine("Dispatcher protocol versions: %s".formatted(Arrays.toString(remoteVersions.toArray())));
        if (!remoteVersions.contains(DISPATCHER_PROTOCOL_VERSION)) {
            throw new TS_LibVncDesktopException_UnsupportedProtocolVersion("Dispatcher unsupported protocol versions");
        }
        transport.writeByte(DISPATCHER_PROTOCOL_VERSION); // send protocol version we use (u8)
        transport.writeByte(1).flush(); // send we are the viewer (u8)
        long connectionId = 0;
        var connIdRetriever = protocol.getConnectionIdRetriever();
        if (null == connIdRetriever) {
            throw new IllegalStateException("ConnectionIdRetriever is null");
        }
        var sId = connIdRetriever.getResult();
        if (TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(sId)) {
            throw new TS_LibVncDesktopException_AuthenticationFailed("ConnectionId is empty");
        }
        try {
            connectionId = Long.parseLong(sId);
        } catch (NumberFormatException nfe) {
            throw new TS_LibVncDesktopException_AuthenticationFailed("Wrong ConnectionId");
        }
        if (0 == connectionId) {
            throw new TS_LibVncDesktopException_AuthenticationFailed("ConnectionId have not be equals to zero");
        }
        transport.writeUInt32(connectionId).flush(); // send connectionId (u32)

        transport.writeByte(0); // send UInt8 secret keyword string length. 0 - for none
        // send String (byte array of ASCII characters) - secret keyword.
        // Skip if none
        transport.writeByte(0).flush(); // send UInt8 dispatcher name string length (may equals to 0)
        // send  -> String (byte array of ASCII characters) - dispatcher name.
        // Skip if none
        //logger.fine("Sent: version3, viewer, connectionId: " + connectionId + " secret:0, token: 0");
        var tokenLength = transport.readUInt8(); // receive UInt8 token length
        // receive byte array  - dispatcher token
        var token = transport.readBytes(tokenLength);
        //logger.fine("token: #" + tokenLength + " " + (tokenLength>0?token[0]:"") +(tokenLength>1?token[1]:"")+(tokenLength>2?token[2]:""));
        // receive 0 'keep alive byte' or non zero 'start byte' (1)
        // on keep alive byte send the same to remote
        // on start byte go to starting rfb connection
        int b;
        do {
            b = transport.readByte();
            if (KEEP_ALIVE_BYTE == b) {
                logger.finer("keep-alive");
                transport.writeByte(KEEP_ALIVE_BYTE).flush();
            }
        } while (b != START_BYTE);
        logger.info("Dispatcher handshake completed");
    }

    /**
     * When first 12 bytes sent by server is "TCPDISPATCH\n" this is dispatcher
     * connection
     *
     * @param protocolString string with first 12 bytes sent by server
     * @return true when we connects to dispatcher, not remote rfb server
     */
    private boolean isDispatcherConnection(String protocolString) {
        final var dispatcherDetected = DISPATCHER_PROTOCOL_STRING.equals(protocolString);
        if (dispatcherDetected) {
            logger.info("Dispatcher connection detected");
        }
        return dispatcherDetected;
    }

    /**
     * Take first 12 bytes sent by server and match rfb protocol version. RFB
     * protocol version string is "RFB MMM.mmm\n". Where MMM is major protocol
     * version and mmm is minor one.
     *
     * Side effect: set protocol.isMac when MacOs at other side is detected
     *
     * @param protocolString string with first 12 bytes sent by server
     * @return version of protocol will be used
     */
    private ProtocolVersion matchProtocolVersion(String protocolString) throws TS_LibVncDesktopException_UnsupportedProtocolVersion {
        logger.info("Server protocol string: %s".formatted(protocolString.substring(0, protocolString.length() - 1)));
        var pattern = Pattern.compile(RFB_PROTOCOL_STRING_REGEXP);
        final var matcher = pattern.matcher(protocolString);
        if (!matcher.matches()) {
            throw new TS_LibVncDesktopException_UnsupportedProtocolVersion(
                    "Unsupported protocol version: %s".formatted(protocolString));
        }
        var major = Integer.parseInt(matcher.group(1));
        var minor = Integer.parseInt(matcher.group(2));
        ProtocolVersion ver;
        var isMac = false;
        if (889 == minor) {
            isMac = true;
        }
        if (major < MIN_SUPPORTED_VERSION_MAJOR
                || MIN_SUPPORTED_VERSION_MAJOR == major && minor < MIN_SUPPORTED_VERSION_MINOR) {
            throw new TS_LibVncDesktopException_UnsupportedProtocolVersion(
                    "Unsupported protocol version: " + major + "." + minor);
        }
        if (major > MAX_SUPPORTED_VERSION_MAJOR) {
//            major = MAX_SUPPORTED_VERSION_MAJOR;
            minor = MAX_SUPPORTED_VERSION_MINOR;
        }

        if (minor >= MIN_SUPPORTED_VERSION_MINOR && minor < 7) {
            ver = ProtocolVersion.PROTOCOL_VERSION_3_3;
        } else if (7 == minor) {
            ver = ProtocolVersion.PROTOCOL_VERSION_3_7;
        } else if (minor >= MAX_SUPPORTED_VERSION_MINOR) {
            ver = ProtocolVersion.PROTOCOL_VERSION_3_8;
        } else {
            throw new TS_LibVncDesktopException_UnsupportedProtocolVersion("Unsupported protocol version: " + protocolString);
        }
        protocol.setMac(isMac);
        return ver;
    }

    private TS_LibVncDesktopTransport_Transport auth(TS_LibVncDesktopTransport_Transport transport, ProtocolVersion ver) throws TS_LibVncDesktopException_UnsupportedSecurityType, TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal, TS_LibVncDesktopException_AuthenticationFailed {
        TS_LibVncDesktopRfbProtocol_AuthHandler handler;
        switch (ver) {
            case PROTOCOL_VERSION_3_3 -> handler = auth33(transport);
            case PROTOCOL_VERSION_3_7 -> handler = auth37_38(transport);
            case PROTOCOL_VERSION_3_8 -> handler = auth37_38(transport);
            default -> throw new IllegalStateException();
        }
        transport = handler.authenticate(transport, protocol);
        if (ver == ProtocolVersion.PROTOCOL_VERSION_3_8
                || handler.getType() != TS_LibVncDesktopRfbProtocol_AuthTypes.NONE_AUTHENTICATION) {
            handler.checkSecurityResult(transport);
        }
        handler.initProcedure(transport, protocol);
        return transport;
    }

    private TS_LibVncDesktopRfbProtocol_AuthHandler auth33(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_UnsupportedSecurityType {
        var type = transport.readInt32();
        logger.info("Type received: %d".formatted(type));
        if (0 == type) {
            throw new TS_LibVncDesktopException_UnsupportedSecurityType(transport.readString());
        }
        return registeredAuthHandlers.get(selectAuthHandlerId((byte) (0xff & type)));
    }

    private TS_LibVncDesktopRfbProtocol_AuthHandler auth37_38(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_UnsupportedSecurityType {
        var secTypesNum = transport.readUInt8();
        if (0 == secTypesNum) {
            throw new TS_LibVncDesktopException_UnsupportedSecurityType(transport.readString());
        }
        var secTypes = transport.readBytes(secTypesNum);
        logger.info("Security Types received (%d): %s".formatted(secTypesNum,TS_LibVncDesktopUtils_Strings.toString(secTypes)));
        final var typeIdAccepted = selectAuthHandlerId(secTypes);
        final var authHandler = registeredAuthHandlers.get(typeIdAccepted);
        transport.writeByte(typeIdAccepted).flush();
        return authHandler;
    }

    private int selectAuthHandlerId(byte... secTypes)
            throws TS_LibVncDesktopException_UnsupportedSecurityType, TS_LibVncDesktopException_Transport {
        TS_LibVncDesktopRfbProtocol_AuthHandler handler;
        // Tight2 Authentication very first
        for (var type : secTypes) {
            if (TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT2_AUTHENTICATION.getId() == (0xff & type)) {
                handler = registeredAuthHandlers.get(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT2_AUTHENTICATION.getId());
                if (handler != null) {
                    logger.info("Security Type accepted(TIGHT2): %s".formatted(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT2_AUTHENTICATION.name()));
                    return TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT2_AUTHENTICATION.getId();
                }
            }
        }
        // Tight Authentication first
        for (var type : secTypes) {
            if (TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION.getId() == (0xff & type)) {
                handler = registeredAuthHandlers.get(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION.getId());
                if (handler != null) {
                    logger.info("Security Type accepted(TIGHT): %s".formatted(TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION.name()));
                    return TS_LibVncDesktopRfbProtocol_AuthTypes.TIGHT_AUTHENTICATION.getId();
                }
            }
        }
        for (var type : secTypes) {
            handler = registeredAuthHandlers.get(0xff & type);
            if (handler != null) {
                logger.info("Security Type accepted(OTHER): %s".formatted(handler.getType().toString()));
                return handler.getType().getId();
            }
        }
        throw new TS_LibVncDesktopException_UnsupportedSecurityType(
                "No security types supported. Server sent '"
                + TS_LibVncDesktopUtils_Strings.toString(secTypes)
                + "' security types, but we do not support any of their.");
    }

    private void registerAuthHandler(int id, TS_LibVncDesktopRfbProtocol_AuthHandler handler) {
        registeredAuthHandlers.put(id, handler);
    }

    public static enum ProtocolVersion {
        PROTOCOL_VERSION_3_3(3, 3),
        PROTOCOL_VERSION_3_7(3, 7),
        PROTOCOL_VERSION_3_8(3, 8);

        public final int minor;
        public final int major;

        ProtocolVersion(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        @Override
        public String toString() {
            return String.valueOf(major) + "." + String.valueOf(minor);
        }
    }
}
