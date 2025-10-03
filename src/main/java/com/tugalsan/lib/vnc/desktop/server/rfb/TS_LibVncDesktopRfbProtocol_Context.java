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

import com.tugalsan.lib.vnc.desktop.server.base.*;

public class TS_LibVncDesktopRfbProtocol_Context {

    int fbWidth;
    int fbHeight;
    TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat;
    TS_LibVncDesktopTransport_Transport transport;
    String remoteDesktopName;
    boolean isTight;
    TS_LibVncDesktopRfb_HandlerHandshaker.ProtocolVersion protocolVersion;
    TS_LibVncDesktopRfbProtocol_Settings settings;
    private TS_LibVncDesktopRfbProtocol_TunnelType tunnelType;

    public TS_LibVncDesktopRfbEncoding_PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public String getRemoteDesktopName() {
        return remoteDesktopName;
    }

    public void setRemoteDesktopName(String name) {
        remoteDesktopName = name;
    }

    public int getFbWidth() {
        return fbWidth;
    }

    public void setFbWidth(int fbWidth) {
        this.fbWidth = fbWidth;
    }

    public int getFbHeight() {
        return fbHeight;
    }

    public void setFbHeight(int fbHeight) {
        this.fbHeight = fbHeight;
    }

    public TS_LibVncDesktopRfbProtocol_Settings getSettings() {
        return settings;
    }

    public TS_LibVncDesktopTransport_Transport getTransport() {
        return transport;
    }

    public void setTight(boolean isTight) {
        this.isTight = isTight;
    }

    public boolean isTight() {
        return isTight;
    }

    public void setProtocolVersion(TS_LibVncDesktopRfb_HandlerHandshaker.ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public TS_LibVncDesktopRfb_HandlerHandshaker.ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public void setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType tunnelType) {
        this.tunnelType = tunnelType;
    }

    public TS_LibVncDesktopRfbProtocol_TunnelType getTunnelType() {
        return tunnelType;
    }
}
