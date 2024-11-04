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
package com.tugalsan.lib.vnc.desktop.server.core;

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_PixelFormat;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Render bitmap data
 *
 * @author dime @ tightvnc.com
 */
public abstract class TS_LibVncDesktopDrawing_Renderer {

    protected final ReentrantLock lock = new ReentrantLock();

    public abstract void drawJpegImage(byte[] bytes, int offset,
            int jpegBufferLength, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect);

    protected int width;
    protected int height;
    protected int[] pixels;
    protected TS_LibVncDesktopDrawing_SoftCursor cursor;
    protected TS_LibVncDesktopDrawing_ColorDecoder colorDecoder;

    final protected void init(int width, int height, TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        this.width = width;
        this.height = height;
        initColorDecoder(pixelFormat);
        pixels = new int[width * height];
        Arrays.fill(pixels, 0);
    }

    public void initColorDecoder(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        lock.lock();
        try {
            colorDecoder = new TS_LibVncDesktopDrawing_ColorDecoder(pixelFormat);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Draw byte array bitmap data
     *
     * @param bytes bitmap data
     * @param x bitmap x position
     * @param y bitmap y position
     * @param width bitmap width
     * @param height bitmap height
     */
    public void drawBytes(byte[] bytes, int x, int y, int width, int height) {
        var i = 0;
        lock.lock();
        try {
            for (var ly = y; ly < y + height; ++ly) {
                var end = ly * this.width + x + width;
                for (var pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = getPixelColor(bytes, i);
                    i += colorDecoder.bytesPerPixel;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Draw byte array bitmap data (for ZRLE)
     */
    public int drawCompactBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        var i = offset;
        lock.lock();
        try {
            for (var ly = y; ly < y + height; ++ly) {
                var end = ly * this.width + x + width;
                for (var pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = getCompactPixelColor(bytes, i);
                    i += colorDecoder.bytesPerCPixel;
                }
            }
        } finally {
            lock.unlock();
        }
        return i - offset;
    }

    /**
     * Draw int (colors) array bitmap data (for ZRLE)
     */
    public void drawColoredBitmap(int[] colors, int x, int y, int width, int height) {
        var i = 0;
        lock.lock();
        try {
            for (var ly = y; ly < y + height; ++ly) {
                var end = ly * this.width + x + width;
                for (var pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = colors[i++];
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Draw byte array bitmap data (for Tight)
     */
    public int drawTightBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        var i = offset;
        lock.lock();
        try {
            for (var ly = y; ly < y + height; ++ly) {
                var end = ly * this.width + x + width;
                for (var pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = colorDecoder.getTightColor(bytes, i);
                    i += colorDecoder.bytesPerPixelTight;
                }
            }
        } finally {
            lock.unlock();
        }
        return i - offset;
    }

    /**
     * Draw byte array bitmap data (from array with plain RGB color components.
     * Assumed: rrrrrrrr gggggggg bbbbbbbb)
     */
    public void drawUncaliberedRGBLine(byte[] bytes, int x, int y, int width) {
        var end = y * this.width + x + width;
        lock.lock();
        try {
            for (int i = 3, pixelsOffset = y * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                pixels[pixelsOffset]
                        = //					(0xff & bytes[i++]) << 16 |
                        //					(0xff & bytes[i++]) << 8 |
                        //					0xff & bytes[i++];
                        (0xff & 255 * (colorDecoder.redMax & bytes[i++]) / colorDecoder.redMax) << 16
                        | (0xff & 255 * (colorDecoder.greenMax & bytes[i++]) / colorDecoder.greenMax) << 8
                        | 0xff & 255 * (colorDecoder.blueMax & bytes[i++]) / colorDecoder.blueMax;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Draw paletted byte array bitmap data
     *
     * @param buffer bitmap data
     * @param rect bitmap location and dimensions
     * @param palette colour palette
     * @param paletteSize number of colors in palette
     */
    public void drawBytesWithPalette(byte[] buffer, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect, int[] palette, int paletteSize) {
        lock.lock();
        try {
            // 2 colors
            if (2 == paletteSize) {
                int dx, dy, n;
                var i = rect.y * this.width + rect.x;
                var rowBytes = (rect.width + 7) / 8;
                byte b;
                
                for (dy = 0; dy < rect.height; dy++) {
                    for (dx = 0; dx < rect.width / 8; dx++) {
                        b = buffer[dy * rowBytes + dx];
                        for (n = 7; n >= 0; n--) {
                            pixels[i++] = palette[b >> n & 1];
                        }
                    }
                    for (n = 7; n >= 8 - rect.width % 8; n--) {
                        pixels[i++] = palette[buffer[dy * rowBytes + dx] >> n & 1];
                    }
                    i += this.width - rect.width;
                }
            } else {
                // 3..255 colors (assuming bytesPixel == 4).
                var i = 0;
                for (var ly = rect.y; ly < rect.y + rect.height; ++ly) {
                    for (var lx = rect.x; lx < rect.x + rect.width; ++lx) {
                        var pixelsOffset = ly * this.width + lx;
                        pixels[pixelsOffset] = palette[buffer[i++] & 0xFF];
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Copy rectangle region from one position to another. Regions may be
     * overlapped.
     *
     * @param srcX source rectangle x position
     * @param srcY source rectangle y position
     * @param dstRect destination rectangle
     */
    public void copyRect(int srcX, int srcY, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle dstRect) {
        int startSrcY, endSrcY, dstY, deltaY;
        if (srcY > dstRect.y) {
            startSrcY = srcY;
            endSrcY = srcY + dstRect.height;
            dstY = dstRect.y;
            deltaY = +1;
        } else {
            startSrcY = srcY + dstRect.height - 1;
            endSrcY = srcY - 1;
            dstY = dstRect.y + dstRect.height - 1;
            deltaY = -1;
        }
        lock.lock();
        try {
            for (var y = startSrcY; y != endSrcY; y += deltaY) {
                System.arraycopy(pixels, y * width + srcX,
                        pixels, dstY * width + dstRect.x, dstRect.width);
                dstY += deltaY;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Fill rectangle region with specified colour
     *
     * @param color colour to fill with
     * @param rect rectangle region positions and dimensions
     */
    public void fillRect(int color, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) {
        fillRect(color, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Fill rectangle region with specified colour
     *
     * @param color colour to fill with
     * @param x rectangle x position
     * @param y rectangle y position
     * @param width rectangle width
     * @param height rectangle height
     */
    public void fillRect(int color, int x, int y, int width, int height) {
        lock.lock();
        try {
            var sy = y * this.width + x;
            var ey = sy + height * this.width;
            for (var i = sy; i < ey; i += this.width) {
                Arrays.fill(pixels, i, i + width, color);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reads color bytes (PIXEL) from transport, returns int combined RGB value
     * consisting of the red component in bits 16-23, the green component in
     * bits 8-15, and the blue component in bits 0-7. May be used directly for
     * creation awt.Color object
     */
    public int readPixelColor(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        return colorDecoder.readColor(transport);
    }

    public int readTightPixelColor(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        return colorDecoder.readTightColor(transport);
    }

    public TS_LibVncDesktopDrawing_ColorDecoder getColorDecoder() {
        return colorDecoder;
    }

    public int getCompactPixelColor(byte[] bytes, int offset) {
        return colorDecoder.getCompactColor(bytes, offset);
    }

    public int getPixelColor(byte[] bytes, int offset) {
        return colorDecoder.getColor(bytes, offset);
    }

    public int getBytesPerPixel() {
        return colorDecoder.bytesPerPixel;
    }

    public int getBytesPerCPixel() {
        return colorDecoder.bytesPerCPixel;
    }

    public int getBytesPerPixelTight() {
        return colorDecoder.bytesPerPixelTight;
    }

    public void fillColorBitmapWithColor(int[] bitmapData, int decodedOffset, int rlength, int color) {
        while (rlength-- > 0) {
            bitmapData[decodedOffset++] = color;
        }
    }

    /**
     * Width of rendered image
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Height of rendered image
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Read and decode cursor image
     *
     * @param rect new cursor hot point position and cursor dimensions
     * @throws TS_LibVncDesktopException_Transport
     */
    public void createCursor(int[] cursorPixels, TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect)
            throws TS_LibVncDesktopException_Transport {
        synchronized (cursor.getLock()) {
            cursor.createCursor(cursorPixels, rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * Read and decode new cursor position
     *
     * @param rect cursor position
     */
    public void decodeCursorPosition(TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) {
        synchronized (cursor.getLock()) {
            cursor.updatePosition(rect.x, rect.y);
        }
    }

}
