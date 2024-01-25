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

import com.tugalsan.lib.vnc.desktop.server.drawing.Renderer;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

public class RawDecoder extends Decoder {

    private static final RawDecoder instance = new RawDecoder();

    public static RawDecoder getInstance() {
        return instance;
    }

    private RawDecoder() {
        /*empty*/ }

    @Override
    public void decode(Transport transport,
            Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        decode(transport, renderer, rect.x, rect.y, rect.width, rect.height);
    }

    public void decode(Transport transport, Renderer renderer, int x, int y,
            int width, int height) throws TransportException {
        var length = width * height * renderer.getBytesPerPixel();
        var bytes = ByteBuffer.getInstance().getBuffer(length);
        transport.readBytes(bytes, 0, length);
        renderer.drawBytes(bytes, x, y, width, height);
    }

}
