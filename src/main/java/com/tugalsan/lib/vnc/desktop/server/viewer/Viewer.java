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

import com.tugalsan.lib.vnc.desktop.server.viewer.swing.SwingViewerWindowFactory;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ConnectionPresenter;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ViewerEventsListener;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.SwingConnectionWorkerFactory;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.SwingViewerWindow;
import com.tugalsan.api.desktop.server.TS_DesktopPaneUtils;
import com.tugalsan.api.desktop.server.TS_DesktopDialogMessageUtils;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.ProtocolSettings;
import com.tugalsan.lib.vnc.desktop.server.utils.LazyLoaded;
import com.tugalsan.lib.vnc.desktop.server.viewer.cli.Parser;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.ConnectionParams;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.UiSettings;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.ConnectionDialogView;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.ConnectionInfoView;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.ConnectionView;
import com.tugalsan.api.thread.server.*;
import com.tugalsan.api.unsafe.client.*;
import java.awt.Window;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;

@SuppressWarnings("serial")
public class Viewer implements ViewerEventsListener {

    private final ApplicationSettings applicationSettings;
    private static final Logger logger = Logger.getLogger(Viewer.class.getName());
    private int paramsMask;
    public final ConnectionParams connectionParams;
    private String passwordFromParams;
    private final ProtocolSettings settings;
    private final UiSettings uiSettings;
    private ConnectionPresenter connectionPresenter;
    private JDesktopPane pane;

//    abstract public void afterLoaded();
    public Viewer(Parser parser, JDesktopPane pane, Window window) {
        this.pane = pane;
        logger.info("TightVNC Viewer version " + ver());
        connectionParams = new ConnectionParams();
        settings = ProtocolSettings.getDefaultSettings();
        uiSettings = new UiSettings();
        applicationSettings = new ApplicationSettings();
        paramsMask = ParametersHandler.completeSettingsFromCLI(parser, connectionParams, settings, uiSettings, applicationSettings);
        setLoggingLevel(applicationSettings.logLevel);
        passwordFromParams = applicationSettings.password;

        connectionPresenter = new ConnectionPresenter(this);
        connectionPresenter.addModel("ConnectionParamsModel", connectionParams);
        final ConnectionView connectionView;
        connectionView = uiSettings.showConnectionDialog ? new ConnectionDialogView(Viewer.this, connectionPresenter, pane, window) : new ConnectionInfoView(Viewer.this, connectionPresenter);
        if (uiSettings.showConnectionDialog) {
            connectionDialogView = (ConnectionDialogView) connectionView;
//            connectionView.getFrame().addInternalFrameListener(new InternalFrameAdapter() {
//                @Override
//                public void internalFrameOpened(InternalFrameEvent e) {
//                    afterLoaded();
//                }
//            });
        }
        connectionPresenter.addView(ConnectionPresenter.CONNECTION_VIEW, connectionView);
        connectionView.getFrame().setBounds(0, 0, 500, 300);

        SwingViewerWindowFactory viewerWindowFactory = new SwingViewerWindowFactory(this);

        connectionPresenter.setConnectionWorkerFactory(new SwingConnectionWorkerFactory(window, passwordFromParams, connectionPresenter, viewerWindowFactory, pane, window));
        connectionPresenter.startConnection(settings, uiSettings, paramsMask);
    }
    public ConnectionDialogView connectionDialogView;

    @Deprecated
    public void connectAction(String remoteHost, int vncPort, boolean useSSHTunnel, String SSHServer, int SSHport, String SSHUSer) {
        if (connectionDialogView != null) {
            connectionDialogView.serverNameField.setText(remoteHost);
            connectionDialogView.setPortNumber(vncPort);
            connectionDialogView.setUseSsh(true);
            connectionDialogView.setSshHostName(SSHServer);
            connectionDialogView.setSshPortNumber(SSHport);
            connectionDialogView.setSshUserName(SSHUSer);
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
        final Logger appLogger = Logger.getLogger("com.glavsoft");
        try {
            appLogger.setUseParentHandlers(false);
            appLogger.setLevel(levelToSet);
            for (Handler h : appLogger.getHandlers()) {
                if (h instanceof ConsoleHandler) {
                    appLogger.removeHandler(h);
                } else {
                    h.setLevel(levelToSet);
                }
            }
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(levelToSet);
            appLogger.addHandler(ch);
        } catch (SecurityException e) {
            logger.warning("cannot set logging level to: " + levelToSet);
        }
    }

    /**
     * Closes App(lication) or stops App(let).
     */
    private void closeApp() {
        if (connectionPresenter != null) {
            connectionPresenter.cancelConnection();
            logger.info("Connection cancelled.");
        }
//        System.exit(0);
    }

    private static LazyLoaded<String> ver = new LazyLoaded<String>(new LazyLoaded.Loader<String>() {
        @Override
        public String load() {
            String version = Viewer.class.getPackage().getImplementationVersion();

            if (version != null) {
                return version;
            }
            try {
                String result;
                Attributes attrs = new Manifest(new FileInputStream(JarFile.MANIFEST_NAME)).getMainAttributes();
                final String ver = attrs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                try {
                    result = ver != null ? new String(ver.getBytes("ISO-8859-1"), "ISO-8859-1") : null;
                } catch (UnsupportedEncodingException e) {
                    result = null;
                }
                version = result;
            } catch (FileNotFoundException e) {
                System.out.println("Manifest file not found");
            } catch (IOException e) {
                System.out.println("Cannot read Manifest file");
            }
            return version;
        }
    });

    public static String ver() {
        return ver.get();
    }

    @Override
    public void onViewerComponentClosing() {
        closeApp();
    }

    @Override
    public void onViewerComponentContainerBuilt(SwingViewerWindow viewerWindow) {
        viewerWindow.setVisible();
        viewerWindow.validate();
        viewerWindow.setZoomToFitSelected(true);
        TS_DesktopPaneUtils.remove(pane, connectionDialogView.getFrame());
        TS_DesktopPaneUtils.tiltWindows(pane);
        TS_ThreadRun.now(() -> {
            TS_ThreadWait.seconds(null, 5);
            TGS_UnSafe.execute(() -> {
                viewerWindow.ReDrawOnResize();
                viewerWindow.getFrame().setResizable(true);
                System.out.println("np");
            }, e -> System.out.println("Jframe is null: " + viewerWindow.getFrame()));
        });
    }
}
