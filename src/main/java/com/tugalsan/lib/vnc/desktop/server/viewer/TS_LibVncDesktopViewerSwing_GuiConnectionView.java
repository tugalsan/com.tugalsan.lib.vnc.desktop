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
package com.tugalsan.lib.vnc.desktop.server.viewer;


import javax.swing.*;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_MvpView;

/**
 * @author dime at tightvnc.com
 */
public interface TS_LibVncDesktopViewerSwing_GuiConnectionView extends TS_LibVncDesktopViewer_MvpView {

    void showReconnectDialog(String title, String message);

    void showConnectionErrorDialog(String message);

    void closeApp();

    JInternalFrame getFrame();
}
