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
package com.tugalsan.lib.vnc.desktop.server.viewer.swing;

import com.tugalsan.lib.vnc.desktop.server.drawing.Renderer;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.PixelFormat;
import com.tugalsan.lib.vnc.desktop.server.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

import java.awt.*;
import java.awt.image.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RendererImpl extends Renderer implements ImageObserver {

    CyclicBarrier barrier = new CyclicBarrier(2);
    private final Image offscreenImage;

    public RendererImpl(Transport transport, int width, int height, PixelFormat pixelFormat) {
        if (0 == width) {
            width = 1;
        }
        if (0 == height) {
            height = 1;
        }
        init(width, height, pixelFormat);
        var colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        var sampleModel = colorModel.createCompatibleSampleModel(width,
                height);

        var dataBuffer = new DataBufferInt(pixels, width * height);
        var raster = Raster.createWritableRaster(sampleModel,
                dataBuffer, null);
        offscreenImage = new BufferedImage(colorModel, raster, false, null);
        cursor = new SoftCursorImpl(0, 0, 0, 0);
    }

    /**
     * Draw jpeg image data
     *
     * @param bytes jpeg image data array
     * @param offset start offset at data array
     * @param jpegBufferLength jpeg image data array length
     * @param rect image location and dimensions
     */
    @Override
    public void drawJpegImage(byte[] bytes, int offset, int jpegBufferLength,
            FramebufferUpdateRectangle rect) {
        var jpegImage = Toolkit.getDefaultToolkit().createImage(bytes,
                offset, jpegBufferLength);
        Toolkit.getDefaultToolkit().prepareImage(jpegImage, -1, -1, this);
        try {
            barrier.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            // nop
        }
        // nop
        // nop
        var graphics = offscreenImage.getGraphics();
        graphics.drawImage(jpegImage, rect.x, rect.y, rect.width, rect.height, this);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
        var isReady = (infoflags & (ALLBITS | ABORT)) != 0;
        if (isReady) {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                // nop
            }
            // nop
            
        }
        return !isReady;
    }

    /* Swing specific interface */
    public Image getOffscreenImage() {
        return offscreenImage;
    }

    public void paintImageOn(Graphics g) {
        lock.lock();
        try {
            if (offscreenImage != null) {
                g.drawImage(offscreenImage, 0, 0, null);
            }
        } finally {
            lock.unlock();
        }
    }

    public void paintCursorOn(Graphics g, boolean force) {
        synchronized (cursor.getLock()) {
            var cursorImage = ((SoftCursorImpl) cursor).getImage();
            if (cursorImage != null && (force
                    || g.getClipBounds().intersects(cursor.rX, cursor.rY, cursor.width, cursor.height))) {
                g.drawImage(cursorImage, cursor.rX, cursor.rY, null);
            }
        }
    }

    public SoftCursorImpl getCursor() {
        return (SoftCursorImpl) cursor;
    }

}
