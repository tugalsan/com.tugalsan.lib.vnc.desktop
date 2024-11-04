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

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_PixelFormat;

public class TS_LibVncDesktopDrawing_ColorDecoder {

    protected byte redShift;
    protected byte greenShift;
    protected byte blueShift;
    public short redMax;
    public short greenMax;
    public short blueMax;
    public final int bytesPerPixel;
    public final int bytesPerCPixel;
    public final int bytesPerPixelTight;
    private final byte[] buff;

    private final int startShift;
    private final int startShiftCompact;
    private final int addShiftItem;
    private final boolean isTightSpecific;

    public TS_LibVncDesktopDrawing_ColorDecoder(TS_LibVncDesktopRfbEncoding_PixelFormat pf) {
        redShift = pf.redShift;
        greenShift = pf.greenShift;
        blueShift = pf.blueShift;
        redMax = pf.redMax;
        greenMax = pf.greenMax;
        blueMax = pf.blueMax;
        bytesPerPixel = pf.bitsPerPixel / 8;
        final long significant = redMax << redShift | greenMax << greenShift | blueMax << blueShift;
        bytesPerCPixel = pf.depth <= 24 // as in RFB
                //                          || 32 == pf.depth) // UltraVNC use this... :(
                && 32 == pf.bitsPerPixel
                && ((significant & 0x00ff000000L) == 0 || (significant & 0x000000ffL) == 0)
                        ? 3
                        : bytesPerPixel;
        bytesPerPixelTight = 24 == pf.depth && 32 == pf.bitsPerPixel ? 3 : bytesPerPixel;
        buff = new byte[bytesPerPixel];
        if (0 == pf.bigEndianFlag) {
            startShift = 0;
            startShiftCompact = 0;
            addShiftItem = 8;
        } else {
            startShift = pf.bitsPerPixel - 8;
            startShiftCompact = Math.max(0, pf.depth - 8);
            addShiftItem = -8;
        }
        isTightSpecific = 4 == bytesPerPixel && 3 == bytesPerPixelTight
                && 255 == redMax && 255 == greenMax && 255 == blueMax;
    }

    protected int readColor(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        return getColor(transport.readBytes(buff, 0, bytesPerPixel), 0);
    }

    protected int readCompactColor(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        return getCompactColor(transport.readBytes(buff, 0, bytesPerCPixel), 0);
    }

    protected int readTightColor(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        return getTightColor(transport.readBytes(buff, 0, bytesPerPixelTight), 0);
    }

    /**
     * Convert rfb encoded pixel color into 0x00rrggbb int value.
     *
     * @param rawColor - bytes are ordered in right sequence (little/big endian
     * transformations already done
     * @return 0x00rrggbb
     */
    protected int convertColor(int rawColor) {
        return 255 * (rawColor >> redShift & redMax) / redMax << 16
                | 255 * (rawColor >> greenShift & greenMax) / greenMax << 8
                | 255 * (rawColor >> blueShift & blueMax) / blueMax;
    }

    public void fillRawComponents(byte[] comp, byte[] bytes, int offset) {
        var rawColor = getRawTightColor(bytes, offset);
        comp[0] = (byte) (rawColor >> redShift & redMax);
        comp[1] = (byte) (rawColor >> greenShift & greenMax);
        comp[2] = (byte) (rawColor >> blueShift & blueMax);
    }

    public int getTightColor(byte[] bytes, int offset) {
        return convertColor(getRawTightColor(bytes, offset));
    }

    private int getRawTightColor(byte[] bytes, int offset) {
        if (isTightSpecific) {
            return (bytes[offset++] & 0xff) << 16
                    | (bytes[offset++] & 0xff) << 8
                    | bytes[offset] & 0xff;
        } else {
            return getRawColor(bytes, offset);
        }
    }

    protected int getColor(byte[] bytes, int offset) {
        return convertColor(getRawColor(bytes, offset));
    }

    private int getRawColor(byte[] bytes, int offset) {
        var shift = startShift;
        var item = addShiftItem;
        var rawColor = (bytes[offset++] & 0xff) << shift;
        for (var i = 1; i < bytesPerPixel; ++i) {
            rawColor |= (bytes[offset++] & 0xff) << (shift += item);
        }
        return rawColor;
    }

    protected int getCompactColor(byte[] bytes, int offset) {
        var shift = startShiftCompact;
        var item = addShiftItem;
        var rawColor = (bytes[offset++] & 0xff) << shift;
        for (var i = 1; i < bytesPerCPixel; ++i) {
            rawColor |= (bytes[offset++] & 0xff) << (shift += item);
        }
        return convertColor(rawColor);
    }

}
