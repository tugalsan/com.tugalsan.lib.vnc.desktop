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

import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopDrawing_Renderer;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopTransport_Transport;

public class TS_LibVncDesktopRfbEncoding_DecoderRaw extends TS_LibVncDesktopRfbEncoding_Decoder {

    private static final TS_LibVncDesktopRfbEncoding_DecoderRaw instance = new TS_LibVncDesktopRfbEncoding_DecoderRaw();

    public static TS_LibVncDesktopRfbEncoding_DecoderRaw getInstance() {
        return instance;
    }

    private TS_LibVncDesktopRfbEncoding_DecoderRaw() {
        /*empty*/ }

    @Override
    public void decode(TS_LibVncDesktopTransport_Transport transport,
            TS_LibVncDesktopDrawing_Renderer renderer, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) throws TS_LibVncDesktopException_Transport {
        decode(transport, renderer, rect.x, rect.y, rect.width, rect.height);
    }

    public void decode(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopDrawing_Renderer renderer, int x, int y,
            int width, int height) throws TS_LibVncDesktopException_Transport {
        var length = width * height * renderer.getBytesPerPixel();
        var bytes = TS_LibVncDesktopRfbEncoding_ByteBuffer.getInstance().getBuffer(length);
        transport.readBytes(bytes, 0, length);
        renderer.drawBytes(bytes, x, y, width, height);
    }

}
