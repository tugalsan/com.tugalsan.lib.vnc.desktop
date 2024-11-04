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

public class TS_LibVncDesktopRfbEncoding_DecoderHextile extends TS_LibVncDesktopRfbEncoding_Decoder {

    private static final int DEFAULT_TILE_SIZE = 16;
    private static final int RAW_MASK = 1;
    private static final int BACKGROUND_SPECIFIED_MASK = 2;
    private static final int FOREGROUND_SPECIFIED_MASK = 4;
    private static final int ANY_SUBRECTS_MASK = 8;
    private static final int SUBRECTS_COLOURED_MASK = 16;
    private static final int FG_COLOR_INDEX = 0;
    private static final int BG_COLOR_INDEX = 1;

    @Override
    public void decode(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopDrawing_Renderer renderer,
            TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) throws TS_LibVncDesktopException_Transport {
        if (rect.width == 0 || rect.height == 0) {
            return;
        }
        var colors = new int[]{-1, -1};
        var maxX = rect.x + rect.width;
        var maxY = rect.y + rect.height;
        for (var tileY = rect.y; tileY < maxY;
                tileY += DEFAULT_TILE_SIZE) {
            var tileHeight = Math.min(maxY - tileY, DEFAULT_TILE_SIZE);
            for (var tileX = rect.x; tileX < maxX;
                    tileX += DEFAULT_TILE_SIZE) {
                var tileWidth = Math.min(maxX - tileX, DEFAULT_TILE_SIZE);
                decodeTile(transport, renderer, colors, tileX,
                        tileY, tileWidth, tileHeight);
            }
        }

    }

    private void decodeTile(TS_LibVncDesktopTransport_Transport transport,
            TS_LibVncDesktopDrawing_Renderer renderer, int[] colors,
            int tileX, int tileY, int tileWidth, int tileHeight)
            throws TS_LibVncDesktopException_Transport {
        var subencoding = transport.readUInt8();
        if ((subencoding & RAW_MASK) != 0) {
            TS_LibVncDesktopRfbEncoding_DecoderRaw.getInstance().decode(transport, renderer,
                    tileX, tileY, tileWidth, tileHeight);
            return;
        }

        if ((subencoding & BACKGROUND_SPECIFIED_MASK) != 0) {
            colors[BG_COLOR_INDEX] = renderer.readPixelColor(transport);
        }
        renderer.fillRect(colors[BG_COLOR_INDEX],
                tileX, tileY, tileWidth, tileHeight);

        if ((subencoding & FOREGROUND_SPECIFIED_MASK) != 0) {
            colors[FG_COLOR_INDEX] = renderer.readPixelColor(transport);
        }

        if ((subencoding & ANY_SUBRECTS_MASK) == 0) {
            return;
        }

        var numberOfSubrectangles = transport.readUInt8();
        var colorSpecified = (subencoding & SUBRECTS_COLOURED_MASK) != 0;
        for (var i = 0; i < numberOfSubrectangles; ++i) {
            var color = colorSpecified ? renderer.readPixelColor(transport) : colors[FG_COLOR_INDEX];
            colors[FG_COLOR_INDEX] = color;
            var dimensions = transport.readByte(); // bits 7-4 for x, bits 3-0 for y
            var subtileX = dimensions >> 4 & 0x0f;
            var subtileY = dimensions & 0x0f;
            dimensions = transport.readByte(); // bits 7-4 for w, bits 3-0 for h
            var subtileWidth = 1 + (dimensions >> 4 & 0x0f);
            var subtileHeight = 1 + (dimensions & 0x0f);
            renderer.fillRect(color,
                    tileX + subtileX, tileY + subtileY,
                    subtileWidth, subtileHeight);
        }
    }

}
