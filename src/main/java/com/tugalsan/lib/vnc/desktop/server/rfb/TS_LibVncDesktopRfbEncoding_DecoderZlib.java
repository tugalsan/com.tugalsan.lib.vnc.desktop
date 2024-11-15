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

import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class TS_LibVncDesktopRfbEncoding_DecoderZlib extends TS_LibVncDesktopRfbEncoding_Decoder {

    private Inflater decoder;

    @Override
    public void decode(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopDrawing_Renderer renderer,
            TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) throws TS_LibVncDesktopException_Transport {
        var zippedLength = (int) transport.readUInt32();
        if (0 == zippedLength) {
            return;
        }
        var length = rect.width * rect.height * renderer.getBytesPerPixel();
        var bytes = unzip(transport, zippedLength, length);
        var unzippedReader
                = new TS_LibVncDesktopTransport_Transport(transport.killTrigger,
                        new ByteArrayInputStream(bytes, zippedLength, length));
        TS_LibVncDesktopRfbEncoding_DecoderRaw.getInstance().decode(unzippedReader, renderer, rect);
    }

    protected byte[] unzip(TS_LibVncDesktopTransport_Transport transport, int zippedLength, int length)
            throws TS_LibVncDesktopException_Transport {
        var bytes = TS_LibVncDesktopRfbEncoding_ByteBuffer.getInstance().getBuffer(zippedLength + length);
        transport.readBytes(bytes, 0, zippedLength);
        if (null == decoder) {
            decoder = new Inflater();
        }
        decoder.setInput(bytes, 0, zippedLength);
        try {
            decoder.inflate(bytes, zippedLength, length);
        } catch (DataFormatException e) {
            throw new TS_LibVncDesktopException_Transport("cannot inflate Zlib data", e);
        }
        return bytes;
    }

    @Override
    public void reset() {
        decoder = null;
    }

}
