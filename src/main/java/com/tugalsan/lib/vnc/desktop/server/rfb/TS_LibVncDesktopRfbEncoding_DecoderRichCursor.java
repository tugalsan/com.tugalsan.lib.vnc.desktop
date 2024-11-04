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

import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopDrawing_Renderer;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;

/**
 * Decoder for RichCursor pseudo encoding
 */
public class TS_LibVncDesktopRfbEncoding_DecoderRichCursor extends TS_LibVncDesktopRfbEncoding_Decoder {

    @Override
    public void decode(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopDrawing_Renderer renderer,
            TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) throws TS_LibVncDesktopException_Transport {
        var bytesPerPixel = renderer.getBytesPerPixel();
        var length = rect.width * rect.height * bytesPerPixel;
        if (0 == length) {
            return;
        }
        var buffer = TS_LibVncDesktopRfbEncoding_ByteBuffer.getInstance().getBuffer(length);
        transport.readBytes(buffer, 0, length);

        var sb = new StringBuilder(" ");
        for (var i = 0; i < length; ++i) {
            sb.append(Integer.toHexString(buffer[i] & 0xff)).append(" ");
        }
        var scanLine = (rect.width + 7) / 8;
        var bitmask = new byte[scanLine * rect.height];
        transport.readBytes(bitmask, 0, bitmask.length);

        sb = new StringBuilder(" ");
        for (var aBitmask : bitmask) {
            sb.append(Integer.toHexString(aBitmask & 0xff)).append(" ");
        }
        var cursorPixels = new int[rect.width * rect.height];
        for (var y = 0; y < rect.height; ++y) {
            for (var x = 0; x < rect.width; ++x) {
                var offset = y * rect.width + x;
                cursorPixels[offset] = isBitSet(bitmask[y * scanLine + x / 8], x % 8)
                        ? 0xFF000000 | renderer.getPixelColor(buffer, offset * bytesPerPixel)
                        : 0; // transparent
            }
        }
        renderer.createCursor(cursorPixels, rect);
    }

    private boolean isBitSet(byte aByte, int index) {
        return (aByte & 1 << 7 - index) > 0;
    }

}
