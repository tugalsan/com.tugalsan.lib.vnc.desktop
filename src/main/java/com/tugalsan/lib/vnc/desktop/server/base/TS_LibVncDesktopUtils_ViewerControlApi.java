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
package com.tugalsan.lib.vnc.desktop.server.base;

import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbClient_MessageType;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbClient_ClientToServerMessage;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopUtils_ViewerControlApi {

    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private final TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter;

    public TS_LibVncDesktopUtils_ViewerControlApi(TS_LibVncDesktopRfbProtocol_Protocol protocol, TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter) {
        this.protocol = protocol;
        this.baudrateMeter = baudrateMeter;
        protocol.setBaudrateMeter(baudrateMeter);
    }

    public void sendMessage(TS_LibVncDesktopRfbClient_ClientToServerMessage message) {
        protocol.sendMessage(message);
    }

    public void sendKeepAlive() {
        protocol.sendSupportedEncodingsMessage(protocol.getSettings());
    }

    public void setCompressionLevelTo(int compressionLevel) {
        final var settings = protocol.getSettings();
        settings.setCompressionLevel(compressionLevel);
        settings.fireListeners();
    }

    public void setJpegQualityTo(int jpegQuality) {
        final var settings = protocol.getSettings();
        settings.setJpegQuality(jpegQuality);
        settings.fireListeners();
    }

    public void setViewOnly(boolean isViewOnly) {
        final var settings = protocol.getSettings();
        settings.setViewOnly(isViewOnly);
        settings.fireListeners();
    }

    public int getBaudrate() {
        return baudrateMeter.kBPS();
    }

    /**
     * Check whether remote server is supported for given client-to-server
     * message
     *
     * @param type client-to-server message type to check for
     * @return true when supported
     */
    public boolean isSupported(TS_LibVncDesktopRfbClient_MessageType type) {
        return protocol.isSupported(type);
    }

}
