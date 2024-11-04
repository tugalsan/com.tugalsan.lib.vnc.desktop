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

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_MvpPresenter;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_WorkersAbstractConnectionWorkerFactory;

import java.awt.*;
import javax.swing.JDesktopPane;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_WorkersRfbConnectionWorker;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_WorkersNetworkConnectionWorker;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewerSwing_ConnectionWorkerFactory extends TS_LibVncDesktopViewer_WorkersAbstractConnectionWorkerFactory {

    private Component parent;
    private String predefinedPassword;
    private final TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter;
    private final TS_LibVncDesktopViewerSwing_ViewerWindowFactory viewerWindowFactory;
    private JDesktopPane pane;
    private Window window;
    private TS_ThreadSyncTrigger killTrigger;

    public TS_LibVncDesktopViewerSwing_ConnectionWorkerFactory(TS_ThreadSyncTrigger killTrigger, Component parent, String predefinedPassword, TS_LibVncDesktopViewer_MvpPresenter presenter,
            TS_LibVncDesktopViewerSwing_ViewerWindowFactory viewerWindowFactory, JDesktopPane pane, Window window) {
        this.killTrigger = killTrigger;
        this.parent = parent;
        this.predefinedPassword = predefinedPassword;
        this.presenter = (TS_LibVncDesktopViewerSwing_ConnectionPresenter) presenter;
        this.viewerWindowFactory = viewerWindowFactory;
        this.pane = pane;
        this.window = window;
    }

    public TS_LibVncDesktopViewerSwing_ConnectionWorkerFactory(TS_ThreadSyncTrigger killTrigger, Component parent, TS_LibVncDesktopViewer_MvpPresenter connectionPresenter, TS_LibVncDesktopViewerSwing_ViewerWindowFactory viewerWindowFactory, JDesktopPane pane, Window window) {
        this(killTrigger, parent, "", connectionPresenter, viewerWindowFactory, pane, window);
    }

    @Override
    public TS_LibVncDesktopViewer_WorkersNetworkConnectionWorker createNetworkConnectionWorker() {
        return new TS_LibVncDesktopViewerSwing_NetworkConnectionWorker(parent);
    }

    @Override
    public TS_LibVncDesktopViewer_WorkersRfbConnectionWorker createRfbConnectionWorker() {
        return new TS_LibVncDesktopViewerSwing_RfbConnectionWorker(killTrigger, predefinedPassword, presenter, parent, viewerWindowFactory, pane, window);
    }

    @Override
    public void setPredefinedPassword(String predefinedPassword) {
        this.predefinedPassword = predefinedPassword;
    }
}
