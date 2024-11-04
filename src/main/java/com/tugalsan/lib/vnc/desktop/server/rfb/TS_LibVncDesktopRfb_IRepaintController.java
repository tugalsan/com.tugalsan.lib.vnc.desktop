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
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;

/**
 * Interface for sending repaint event from worker thread to GUI thread
 */
public interface TS_LibVncDesktopRfb_IRepaintController extends TS_LibVncDesktopRfb_IChangeSettingsListener {

    void repaintBitmap(TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect);

    void repaintBitmap(int x, int y, int width, int height);

    void repaintCursor();

    void updateCursorPosition(short x, short y);

    TS_LibVncDesktopDrawing_Renderer createRenderer(TS_LibVncDesktopTransport_Transport transport, int width, int height, TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat);

    void setPixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat);
}
