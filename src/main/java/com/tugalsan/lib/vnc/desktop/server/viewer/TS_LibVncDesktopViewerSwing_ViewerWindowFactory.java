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

import module java.desktop;
import com.tugalsan.lib.vnc.desktop.server.rfb.*;

/**
 * Factory that creates SwingViewerWindow with a number of params
 *
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewerSwing_ViewerWindowFactory {

    private final TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener viewerEventsListener;

    /**
     * Construct factory for creating SwingViewerWindow
     *
     * @param isSeparateFrame if true, creates at the separate frame, else use
     * external container
     * @see #setExternalContainer(Container) and put viewer window into the
     * container
     *
     * @param viewerEventsListener the listener for closing app events
     */
    public TS_LibVncDesktopViewerSwing_ViewerWindowFactory(TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener viewerEventsListener) {
        this.viewerEventsListener = viewerEventsListener;
    }

    /**
     * Creates SwingViewerWindow
     *
     * @see TS_LibVncDesktopViewerSwing_ViewerWindow
     *
     * @param workingProtocol Protocol object, represents network session that
     * is 'connected' to remote host
     * @param rfbSettings rfb protocol settings currently used at the session
     * @param uiSettings gui settings currently used to for displaying viewer
     * window
     * @param connectionString used to show window title string for displaying
     * remote host name
     * @param presenter ConnectionPresenter that response for reconnection and
     * connection history manipulation
     * @return the SwingViewerWindow
     */
    public TS_LibVncDesktopViewerSwing_ViewerWindow createViewerWindow(TS_LibVncDesktopRfbProtocol_Protocol workingProtocol, TS_LibVncDesktopRfbProtocol_Settings rfbSettings, TS_LibVncDesktopViewer_SettingsUi uiSettings, String connectionString, TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter, JDesktopPane pane, Window window) {
        // TODO do we need in presenter here? or split to to history handling and reconnection ability
        var surface = new TS_LibVncDesktopViewerSwing_Surface(workingProtocol, uiSettings.getScaleFactor());
        var viewerWindow = new TS_LibVncDesktopViewerSwing_ViewerWindow(workingProtocol, rfbSettings, uiSettings, surface, viewerEventsListener, connectionString, presenter, pane, window);
        surface.setViewerWindow(viewerWindow);
        viewerWindow.setRemoteDesktopName(workingProtocol.getRemoteDesktopName());
        rfbSettings.addListener(viewerWindow);
        uiSettings.addListener(surface);
        return viewerWindow;
    }
}
