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

import com.tugalsan.lib.vnc.desktop.server.exceptions.*;
import com.tugalsan.lib.vnc.desktop.server.base.*;

/**
 * Header for framebuffer-update-rectangle header server message 2 - U16 -
 * x-position 2 - U16 - y-position 2 - U16 - width 2 - U16 - height 4 - S32 -
 * encoding-type and then follows the pixel data in the specified encoding
 */
public class TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle {

    public int x;
    public int y;
    public int width;
    public int height;
    private TS_LibVncDesktopRfbEncoding_Type encodingType;

    public TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle() {
        // nop
    }

    public TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
    }

    public void fill(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        x = transport.readUInt16();
        y = transport.readUInt16();
        width = transport.readUInt16();
        height = transport.readUInt16();
        var encoding = transport.readInt32();
        encodingType = TS_LibVncDesktopRfbEncoding_Type.byId(encoding);
    }

    public TS_LibVncDesktopRfbEncoding_Type getEncodingType() {
        return encodingType;
    }

    @Override
    public String toString() {
        return "FramebufferUpdateRect: [x: " + x + ", y: " + y
                + ", width: " + width + ", height: " + height
                + ", encodingType: " + encodingType
                + "]";
    }

}
