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

import module com.tugalsan.api.thread;
import module com.tugalsan.api.function;
import module java.desktop;
import module java.logging;
import com.tugalsan.lib.vnc.desktop.server.rfb.*;
import com.tugalsan.lib.vnc.desktop.server.base.*;
import com.tugalsan.lib.vnc.desktop.server.exceptions.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewerSwing_RfbConnectionWorker extends SwingWorker<List<String>, String> implements TS_LibVncDesktopViewer_WorkersRfbConnectionWorker<List<String>>, TS_LibVncDesktopRfb_ISessionListener {

    private final String predefinedPassword;
    private final TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter;
    private final Component parent;
    private final TS_LibVncDesktopViewerSwing_ViewerWindowFactory viewerWindowFactory;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopViewerSwing_RfbConnectionWorker.class.getName());
    private volatile boolean isStoppingProcess;
    private TS_LibVncDesktopViewerSwing_ViewerWindow viewerWindow;
    private String connectionString;
    private TS_LibVncDesktopRfbProtocol_Protocol workingProtocol;
    private Socket workingSocket;
    private TS_LibVncDesktopRfbProtocol_Settings rfbSettings;
    private TS_LibVncDesktopViewer_SettingsUi uiSettings;
    private TS_LibVncDesktopUtils_ViewerControlApi viewerControlApi;
    private final TS_ThreadSyncTrigger killTrigger;

    public TS_LibVncDesktopViewerSwing_RfbConnectionWorker(TS_ThreadSyncTrigger killTrigger, String predefinedPassword, TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter, Component parent,
            TS_LibVncDesktopViewerSwing_ViewerWindowFactory viewerWindowFactory, JDesktopPane pane, Window window) {
        this.killTrigger = killTrigger;
        this.predefinedPassword = predefinedPassword;
        this.presenter = presenter;
        this.parent = parent;
        this.viewerWindowFactory = viewerWindowFactory;
        this.pane = pane;
        this.window = window;
    }
    private final JDesktopPane pane;
    private final Window window;

    @Override
    protected void process(List<String> strings) { // EDT
        var message = strings.get(strings.size() - 1); // get last
        presenter.showMessage(message);
    }

    @Override
    protected void done() { // EDT
        try {
            var msg = "Handshake established";
            publish(msg);
            presenter.showMessage(msg);
            var clipboardController = new TS_LibVncDesktopViewerSwing_ClipboardControllerImpl(killTrigger, workingProtocol, rfbSettings.getRemoteCharsetName());
            clipboardController.setEnabled(rfbSettings.isAllowClipboardTransfer());
            rfbSettings.addListener(clipboardController);
            viewerWindow = viewerWindowFactory.createViewerWindow(
                    workingProtocol, rfbSettings, uiSettings, connectionString, presenter,
                    pane, window
            );

            workingProtocol.startNormalHandling(killTrigger, this, viewerWindow.getRepaintController(), clipboardController);
            presenter.showMessage("Started");

            presenter.successfulRfbConnection();
        } catch (CancellationException e) {
            logger.info("Cancelled");
            presenter.showMessage("Cancelled");
            presenter.connectionCancelled();
        }
    }

    @Override
    public void rfbSessionStopped(final String reason) {
        if (workingProtocol != null) {
            workingProtocol.cleanUpSession();
        }
        if (isStoppingProcess) {
            return;
        }
        cleanUpUISessionAndConnection();
        logger.info("Rfb session stopped: %s".formatted(reason));
        if (presenter.needReconnection()) {
            SwingUtilities.invokeLater(() -> {
                presenter.showReconnectDialog("Connection error", reason);
                presenter.reconnect(predefinedPassword);
            });
        }
    }

    @Override
    public boolean cancel() {
        var res = super.cancel(true);
        if (res && workingProtocol != null) {
            workingProtocol.cleanUpSession();
        }
        cleanUpUISessionAndConnection();
        return res;
    }

    private void cleanUpUISessionAndConnection() {
        synchronized (this) {
            isStoppingProcess = true;
        }
        if (workingSocket != null && workingSocket.isConnected()) {
            try {
                workingSocket.close();
            } catch (IOException e) {
                /*nop*/ }
        }
        if (viewerWindow != null) {
            viewerWindow.close();
        }
        synchronized (this) {
            isStoppingProcess = false;
        }
    }

    @Override
    public void setWorkingSocket(Socket workingSocket) {
        this.workingSocket = workingSocket;
    }

    @Override
    public void setRfbSettings(TS_LibVncDesktopRfbProtocol_Settings rfbSettings) {
        this.rfbSettings = rfbSettings;
    }

    @Override
    public void setUiSettings(TS_LibVncDesktopViewer_SettingsUi uiSettings) {
        this.uiSettings = uiSettings;
    }

    @Override
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public List<String> doInBackground() throws Exception {
        if (null == workingSocket) {
            throw new TS_LibVncDesktopViewerSwing_ConnectionErrorException("Null socket");
        }
        workingSocket.setTcpNoDelay(true); // disable Nagle algorithm
        var transport = new TS_LibVncDesktopTransport_Transport(killTrigger, workingSocket);
        var baudrateMeter = new TS_LibVncDesktopTransport_BaudrateMeter();
        transport.setBaudrateMeter(baudrateMeter);
        workingProtocol = new TS_LibVncDesktopRfbProtocol_Protocol(transport,
                new PasswordChooser(connectionString, parent, this),
                rfbSettings);
        workingProtocol.setConnectionIdRetriever(new ConnectionIdChooser(parent, this));
        viewerControlApi = new TS_LibVncDesktopUtils_ViewerControlApi(workingProtocol, baudrateMeter);
        var message = "Handshaking with remote host";
        logger.info(message);
        publish(message);
        TGS_FuncMTCUtils.run(() -> {
            try {
                workingProtocol.handshake();
            } catch (TS_LibVncDesktopException_AuthenticationFailed ex) {
                TGS_FuncMTUUtils.thrw(ex);
            } catch (TS_LibVncDesktopException_Transport | TS_LibVncDesktopException_Fatal ex) {
                TGS_FuncMTUUtils.thrw(ex);
            } catch (Throwable ex) {
                TGS_FuncMTUUtils.thrw(ex);
            }
        });
        return List.of();
    }

    /**
     * Ask user for password if needed
     */
    private class PasswordChooser implements TS_LibVncDesktopRfb_IRequestString {

        private final String connectionString;
        private final Component parent;
        private final TS_LibVncDesktopViewer_WorkersConnectionWorker onCancel;

        private PasswordChooser(String connectionString, Component parent, TS_LibVncDesktopViewer_WorkersConnectionWorker onCancel) {
            this.connectionString = connectionString;
            this.parent = parent;
            this.onCancel = onCancel;
        }

        @Override
        public String getResult() {
            return TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(predefinedPassword)
                    ? askPassword()
                    : predefinedPassword;
        }

        private String askPassword() {
            var dialog = new TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog(parent, "VNC Authentication", true,
                    "Server '" + connectionString + "' requires VNC authentication", "Password:")
                    .setOkLabel("Login")
                    .setInputFieldLength(12);
            if (!dialog.askResult()) {
                onCancel.cancel();
            }
            return dialog.getResult();
        }
    }

    @Override
    public TS_LibVncDesktopUtils_ViewerControlApi getViewerControlApi() {
        return viewerControlApi;
    }

    private class ConnectionIdChooser implements TS_LibVncDesktopRfb_IRequestString {

        private final Component parent;
        private final TS_LibVncDesktopViewer_WorkersConnectionWorker<List<String>> onCancel;

        public ConnectionIdChooser(Component parent, TS_LibVncDesktopViewer_WorkersConnectionWorker<List<String>> onCancel) {
            this.parent = parent;
            this.onCancel = onCancel;
        }

        @Override
        public String getResult() {
            var dialog = new TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog(parent, "TcpDispatcher ConnectionId", false,
                    "TcpDispatcher requires Connection Id.",
                    "Please get the Connection Id from you peer by any other communication channel\n(ex. phone call or IM) and insert it into the form field below.",
                    "Connection Id:")
                    .setInputFieldLength(18);
            if (!dialog.askResult()) {
                onCancel.cancel();
            }
            return dialog.getResult();
        }
    }
}
