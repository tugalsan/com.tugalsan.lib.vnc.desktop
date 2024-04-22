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

import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.lib.vnc.desktop.server.exceptions.AuthenticationFailedException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.UnsupportedProtocolVersionException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.UnsupportedSecurityTypeException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.FatalException;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRequestString;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRfbSessionListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.ProtocolSettings;
import com.tugalsan.lib.vnc.desktop.server.transport.BaudrateMeter;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;
import com.tugalsan.lib.vnc.desktop.server.utils.Strings;
import com.tugalsan.lib.vnc.desktop.server.utils.ViewerControlApi;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.UiSettings;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.RequestSomethingDialog;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.ConnectionWorker;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.RfbConnectionWorker;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * @author dime at tightvnc.com
 */
public class SwingRfbConnectionWorker extends SwingWorker<List<String>, String> implements RfbConnectionWorker<List<String>>, IRfbSessionListener {

    final private static TS_Log d = TS_Log.of(true, SwingRfbConnectionWorker.class);

    private final String predefinedPassword;
    private final ConnectionPresenter presenter;
    private final Component parent;
    private final SwingViewerWindowFactory viewerWindowFactory;
    private static final Logger logger = Logger.getLogger(SwingRfbConnectionWorker.class.getName());
    private volatile boolean isStoppingProcess;
    private SwingViewerWindow viewerWindow;
    private String connectionString;
    private Protocol workingProtocol;
    private Socket workingSocket;
    private ProtocolSettings rfbSettings;
    private UiSettings uiSettings;
    private ViewerControlApi viewerControlApi;
    private final TS_ThreadSyncTrigger killTrigger;

  

    public SwingRfbConnectionWorker(TS_ThreadSyncTrigger killTrigger, String predefinedPassword, ConnectionPresenter presenter, Component parent,
            SwingViewerWindowFactory viewerWindowFactory, JDesktopPane pane, Window window) {
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
        d.ci("done", "#10");
        try {
            d.ci("done", "#11");
            get();
            d.ci("done", "#12");
            presenter.showMessage("Handshake established");
            var clipboardController = new ClipboardControllerImpl(killTrigger, workingProtocol, rfbSettings.getRemoteCharsetName());
            clipboardController.setEnabled(rfbSettings.isAllowClipboardTransfer());
            rfbSettings.addListener(clipboardController);
            viewerWindow = viewerWindowFactory.createViewerWindow(
                    workingProtocol, rfbSettings, uiSettings, connectionString, presenter,
                    pane, window
            );

            workingProtocol.startNormalHandling(killTrigger, this, viewerWindow.getRepaintController(), clipboardController);
            presenter.showMessage("Started");

            d.ci("done", "#20");
            presenter.successfulRfbConnection();
            d.ci("done", "#21");
        } catch (CancellationException e) {
            d.ci("done", "#21.e1");
            logger.info("Cancelled");
            presenter.showMessage("Cancelled");
            presenter.connectionCancelled();
        } catch (InterruptedException e) {
            d.ci("done", "#21.e2");
            logger.info("Interrupted");
            presenter.showMessage("Interrupted");
            presenter.connectionFailed();
        } catch (ExecutionException ee) {
            d.ci("done", "#21.e3 begin");
            String errorTitle;
            String errorMessage;
            try {
                throw ee.getCause();
            } catch (UnsupportedProtocolVersionException e) {
                errorTitle = "Unsupported Protocol Version";
                errorMessage = e.getMessage();
                logger.severe("%s:%s".formatted(errorTitle, errorMessage));
            } catch (UnsupportedSecurityTypeException e) {
                errorTitle = "Unsupported Security Type";
                errorMessage = e.getMessage();
                logger.severe("%s:%s".formatted(errorTitle, errorMessage));
            } catch (AuthenticationFailedException e) {
                errorTitle = "Authentication Failed";
                errorMessage = e.getMessage();
                logger.severe("%s:%s".formatted(errorTitle, errorMessage));
                presenter.clearPredefinedPassword();
            } catch (TransportException e) {
                errorTitle = "Connection Error";
                var cause = e.getCause();
                errorMessage = errorTitle + " : " + e.getMessage();
                if (cause != null) {
                    if (cause instanceof EOFException) {
                        errorMessage += ", possible reason: remote host not responding.";
                    }
                    logger.throwing("", "", cause);
                }
                logger.severe(errorMessage);
            } catch (EOFException e) {
                errorTitle = "Connection Error";
                errorMessage = errorTitle + ": " + e.getMessage();
                logger.severe(errorMessage);
            } catch (IOException e) {
                errorTitle = "Connection Error";
                errorMessage = errorTitle + ":  " + e.getMessage();
                logger.severe(errorMessage);
            } catch (FatalException e) {
                errorTitle = "Connection Error";
                errorMessage = errorTitle + ":    " + e.getMessage();
                logger.severe(errorMessage);
            } catch (Throwable e) {
                errorTitle = "Error";
                errorMessage = errorTitle + ": " + e.getMessage();
                logger.severe(errorMessage);
            }
            presenter.showReconnectDialog(errorTitle, errorMessage);
            presenter.clearMessage();
            presenter.connectionFailed();
            d.ci("done", "#21.e3.end");
        }
        d.ci("done", "#30");
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
    public void setRfbSettings(ProtocolSettings rfbSettings) {
        this.rfbSettings = rfbSettings;
    }

    @Override
    public void setUiSettings(UiSettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    @Override
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public List<String> doInBackground() throws Exception {
        if (null == workingSocket) {
            throw new ConnectionErrorException("Null socket");
        }
        workingSocket.setTcpNoDelay(true); // disable Nagle algorithm
        var transport = new Transport(workingSocket);
        var baudrateMeter = new BaudrateMeter();
        transport.setBaudrateMeter(baudrateMeter);
        workingProtocol = new Protocol(transport,
                new PasswordChooser(connectionString, parent, this),
                rfbSettings);
        workingProtocol.setConnectionIdRetriever(new ConnectionIdChooser(parent, this));
        viewerControlApi = new ViewerControlApi(workingProtocol, baudrateMeter);
        var message = "Handshaking with remote host";
        logger.info(message);
        publish(message);

        d.ci("doInBackground", "handshake begin");
        workingProtocol.handshake();
        d.ci("doInBackground", "handshake end");
        return List.of();
    }

    /**
     * Ask user for password if needed
     */
    private class PasswordChooser implements IRequestString {

        private final String connectionString;
        private final Component parent;
        private final ConnectionWorker onCancel;

        private PasswordChooser(String connectionString, Component parent, ConnectionWorker onCancel) {
            this.connectionString = connectionString;
            this.parent = parent;
            this.onCancel = onCancel;
        }

        @Override
        public String getResult() {
            return Strings.isTrimmedEmpty(predefinedPassword)
                    ? askPassword()
                    : predefinedPassword;
        }

        private String askPassword() {
            var dialog = new RequestSomethingDialog(parent, "VNC Authentication", true,
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
    public ViewerControlApi getViewerControlApi() {
        return viewerControlApi;
    }

    private class ConnectionIdChooser implements IRequestString {

        private final Component parent;
        private final ConnectionWorker<List<String>> onCancel;

        public ConnectionIdChooser(Component parent, ConnectionWorker<List<String>> onCancel) {
            this.parent = parent;
            this.onCancel = onCancel;
        }

        @Override
        public String getResult() {
            var dialog = new RequestSomethingDialog(parent, "TcpDispatcher ConnectionId", false,
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
