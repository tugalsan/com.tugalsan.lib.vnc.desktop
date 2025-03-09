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

import com.tugalsan.api.desktop.server.TS_DesktopDesktopPaneUtils;
import com.tugalsan.api.desktop.server.TS_DesktopDialogMessageUtils;
import com.tugalsan.api.function.client.maythrow.checkedexceptions.TGS_FuncMTCEUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.run.TS_ThreadAsyncRun;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncWait;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Settings;

import java.awt.Window;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;

@SuppressWarnings("serial")
public class TS_LibVncDesktopViewer_Viewer implements TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener {

    final static private TS_Log d = TS_Log.of(TS_LibVncDesktopViewer_Viewer.class);
    private final TS_LibVncDesktopViewer_ApplicationSettings applicationSettings;
    public final TS_ThreadSyncTrigger killTrigger;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopViewer_Viewer.class.getName());
    private final int paramsMask;
    public final TS_LibVncDesktopViewer_SettingsViewerConnectionParams connectionParams;
    private final String passwordFromParams;
    private final TS_LibVncDesktopRfbProtocol_Settings settings;
    private final TS_LibVncDesktopViewer_SettingsUi uiSettings;
    private final TS_LibVncDesktopViewerSwing_ConnectionPresenter connectionPresenter;
    private final JDesktopPane pane;

//    abstract public void afterLoaded();
    public TS_LibVncDesktopViewer_Viewer(TS_ThreadSyncTrigger killTrigger, TS_LibVncDesktopViewer_CliParser parser, JDesktopPane pane, Window window) {
        this.killTrigger = killTrigger;
        this.pane = pane;
        connectionParams = new TS_LibVncDesktopViewer_SettingsViewerConnectionParams();
        settings = TS_LibVncDesktopRfbProtocol_Settings.getDefaultSettings();
        uiSettings = new TS_LibVncDesktopViewer_SettingsUi();
        applicationSettings = new TS_LibVncDesktopViewer_ApplicationSettings();
        paramsMask = TS_LibVncDesktopViewer_ParametersHandler.completeSettingsFromCLI(parser, connectionParams, settings, uiSettings, applicationSettings);
        setLoggingLevel(applicationSettings.logLevel);
        passwordFromParams = applicationSettings.password;

        connectionPresenter = new TS_LibVncDesktopViewerSwing_ConnectionPresenter(this);
        connectionPresenter.addModel("ConnectionParamsModel", connectionParams);
        final TS_LibVncDesktopViewerSwing_GuiConnectionView connectionView;
        connectionView = uiSettings.showConnectionDialog ? new TS_LibVncDesktopViewerSwing_GuiConnectionDialogView(TS_LibVncDesktopViewer_Viewer.this, connectionPresenter, pane, window) : new TS_LibVncDesktopViewerSwing_GuiConnectionInfoView(TS_LibVncDesktopViewer_Viewer.this, connectionPresenter);
        if (uiSettings.showConnectionDialog) {
            connectionDialogView = (TS_LibVncDesktopViewerSwing_GuiConnectionDialogView) connectionView;
//            connectionView.getFrame().addInternalFrameListener(new InternalFrameAdapter() {
//                @Override
//                public void internalFrameOpened(InternalFrameEvent e) {
//                    afterLoaded();
//                }
//            });
        }
        connectionPresenter.addView(TS_LibVncDesktopViewerSwing_ConnectionPresenter.CONNECTION_VIEW, connectionView);
        connectionView.getFrame().setBounds(0, 0, 500, 300);

        var viewerWindowFactory = new TS_LibVncDesktopViewerSwing_ViewerWindowFactory(this);

        connectionPresenter.setConnectionWorkerFactory(new TS_LibVncDesktopViewerSwing_ConnectionWorkerFactory(killTrigger, window, passwordFromParams, connectionPresenter, viewerWindowFactory, pane, window));
        connectionPresenter.startConnection(settings, uiSettings, paramsMask);
    }
    public TS_LibVncDesktopViewerSwing_GuiConnectionDialogView connectionDialogView;

    @Deprecated
    public void connectAction(String remoteHost, int vncPort) {
        if (connectionDialogView != null) {
            connectionDialogView.serverNameField.setText(remoteHost);
            connectionDialogView.setPortNumber(vncPort);
            TS_DesktopDialogMessageUtils.show("TODO CONNECT");
            //connectionDialogView.connectAction();
        } else {
            TS_DesktopDialogMessageUtils.show("Error: connectionView is not instance of ConnectionDialogView");
        }
    }

    public void connectAction() {
        if (connectionDialogView != null) {
            connectionDialogView.connectAction();
        } else {
            TS_DesktopDialogMessageUtils.show("Error: connectionView is not instance of ConnectionDialogView");
        }
    }

    private void setLoggingLevel(Level levelToSet) {
        var appLogger = Logger.getLogger("com.glavsoft");
        try {
            appLogger.setUseParentHandlers(false);
            appLogger.setLevel(levelToSet);
            for (var h : appLogger.getHandlers()) {
                if (h instanceof ConsoleHandler) {
                    appLogger.removeHandler(h);
                } else {
                    h.setLevel(levelToSet);
                }
            }
            var ch = new ConsoleHandler();
            ch.setLevel(levelToSet);
            appLogger.addHandler(ch);
        } catch (SecurityException e) {
            logger.log(Level.WARNING, "cannot set logging level to: {0}", levelToSet);
        }
    }

    private void closeApp() {
        if (connectionPresenter != null) {
            connectionPresenter.cancelConnection();
            logger.info("Connection cancelled.");
        }
//        System.exit(0);
    }

    @Override
    public void onViewerComponentClosing() {
        closeApp();
    }

    @Override
    public void onViewerComponentContainerBuilt(TS_LibVncDesktopViewerSwing_ViewerWindow viewerWindow) {
        viewerWindow.setVisible();
        viewerWindow.validate();
        viewerWindow.setZoomToFitSelected(true);
        TS_DesktopDesktopPaneUtils.remove(pane, connectionDialogView.getFrame());
        TS_DesktopDesktopPaneUtils.tiltWindows(pane);
        TS_ThreadAsyncRun.now(killTrigger.newChild(d.className), kt -> {
            TS_ThreadSyncWait.seconds(null, killTrigger, 5);
            TGS_FuncMTCEUtils.run(() -> {
                viewerWindow.ReDrawOnResize();
                viewerWindow.getFrame().setResizable(true);
                System.out.println("np");
            }, e -> System.out.println("Jframe is null: " + viewerWindow.getFrame()));
        });
    }
}
