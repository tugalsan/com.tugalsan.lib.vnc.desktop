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
package com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder;

import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.EncodingType;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

/**
 * Header for framebuffer-update-rectangle header server message 2 - U16 -
 * x-position 2 - U16 - y-position 2 - U16 - width 2 - U16 - height 4 - S32 -
 * encoding-type and then follows the pixel data in the specified encoding
 */
public class FramebufferUpdateRectangle {

    public int x;
    public int y;
    public int width;
    public int height;
    private EncodingType encodingType;

    public FramebufferUpdateRectangle() {
        // nop
    }

    public FramebufferUpdateRectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
    }

    public void fill(Transport transport) throws TransportException {
        x = transport.readUInt16();
        y = transport.readUInt16();
        width = transport.readUInt16();
        height = transport.readUInt16();
        var encoding = transport.readInt32();
        encodingType = EncodingType.byId(encoding);
    }

    public EncodingType getEncodingType() {
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
