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

public class RREDecoder extends Decoder {

    @Override
    public void decode(Transport transport, Renderer renderer,
            FramebufferUpdateRectangle rect) throws TransportException {
        var numOfSubrectangles = transport.readInt32();
        var color = renderer.readPixelColor(transport);
        renderer.fillRect(color, rect);
        for (var i = 0; i < numOfSubrectangles; ++i) {
            color = renderer.readPixelColor(transport);
            var x = transport.readUInt16();
            var y = transport.readUInt16();
            var width = transport.readUInt16();
            var height = transport.readUInt16();
            renderer.fillRect(color, rect.x + x, rect.y + y, width, height);
        }

    }

}
