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

import com.tugalsan.lib.vnc.desktop.server.viewer.mvp.Presenter;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.ConnectionParams;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ssh.SshConnectionManager;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.NetworkConnectionWorker;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SwingNetworkConnectionWorker extends SwingWorker<Socket, String> implements NetworkConnectionWorker {

    public static final int MAX_HOSTNAME_LENGTH_FOR_MESSAGES = 40;
    private final Component parent;
    private final Logger logger;
    private boolean hasSshSupport;
    private ConnectionParams connectionParams;
    private ConnectionPresenter presenter;
    private SshConnectionManager sshConnectionManager;

    public SwingNetworkConnectionWorker(Component parent) {
        this.parent = parent;
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public Socket doInBackground() throws IOException, InterruptedException, ConnectionErrorException, CancelConnectionException, UnknownHostException {
        return null;
    }

    public Socket doInBackground2() throws IOException, InterruptedException, ConnectionErrorException, CancelConnectionException, UnknownHostException {
        String s = "<b>" + connectionParams.hostName + "</b>:" + connectionParams.getPortNumber();
        if (connectionParams.useSsh()) {
            s += " <i>(via com.glavsoft.viewer.swing.ssh://" + connectionParams.sshUserName + "@" + connectionParams.sshHostName + ":" + connectionParams.getSshPortNumber() + ")</i>";
        }

        String message = "<html>Trying to connect to " + s + "</html>";
        logger.info(message.replaceAll("<[^<>]+?>", ""));
        publish(message);
        int port;
        String host;
        if (hasSshSupport && connectionParams.useSsh()) {
            try {
                sshConnectionManager = SshConnectionManager.createManager(parent);
            } catch (ConnectionErrorException e) {
                hasSshSupport = false; // TODO propogate into upper level?
                throw e;
            }
            message = "Creating SSH tunnel to " + connectionParams.sshHostName + ":" + connectionParams.getSshPortNumber();
            logger.info(message);
            publish(message);
            port = sshConnectionManager.connect(connectionParams);
            if (sshConnectionManager.isConnected()) {
                host = "127.0.0.1";
                message = "SSH tunnel established: " + host + ":" + port;
                logger.info(message);
                publish(message);
            } else {
                throw new ConnectionErrorException("Could not create SSH tunnel: " + sshConnectionManager.getErrorMessage());
            }
        } else {
            host = connectionParams.hostName;
            port = connectionParams.getPortNumber();
        }

        message = "Connecting to host " + host + ":" + port + (connectionParams.useSsh() ? " (tunneled)" : "");
        logger.info(message);
        publish(message);
        try {
            Thread.sleep(30); // throws
        } catch (InterruptedException ie) {
            throw ie;
        }
        logger.info("creating socket...");
        logger.info("host:" + host);
        logger.info("port:" + port);
        return new Socket(host, port);
    }

    private String formatHostString(String hostName) {
        if (hostName.length() <= MAX_HOSTNAME_LENGTH_FOR_MESSAGES) {
            return hostName;
        } else {
            return hostName.substring(0, MAX_HOSTNAME_LENGTH_FOR_MESSAGES) + "...";
        }
    }

    @Override
    protected void process(List<String> strings) { // EDT
        String message = strings.get(strings.size() - 1); // get last
        presenter.showMessage(message);
    }

    @Override
    protected void done() { // EDT
        logger.info("creating socket done.begin");
        try {
            Socket socket = null;
            try {
                Thread.sleep(30); // throws
            } catch (InterruptedException ie) {
                throw ie;
            }
            try {
                socket = doInBackground2();
            } catch (IOException | ConnectionErrorException | CancelConnectionException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(30); // throws
            } catch (InterruptedException ie) {
                throw ie;
            }

            logger.info("creating socket done#0");
            presenter.successfulNetworkConnection(socket);
            logger.info("creating socket done#1");
        } catch (CancellationException e) {
            logger.info("Cancelled: " + e.getMessage());
            e.printStackTrace();
            presenter.showMessage("Cancelled");
            presenter.connectionFailed();
        } catch (InterruptedException e) {
            logger.info("Interrupted");
            presenter.showMessage("Interrupted");
            presenter.connectionFailed();
//        } catch (ExecutionException e) {
//            logger.info("ExecutionException");
//            String errorMessage = null;
//            try {
//                logger.info("ExecutionException will throw cause");
//                throw e.getCause();
//            } catch (UnknownHostException uhe) {
//                logger.severe("ExecutionException.Unknown host: " + connectionParams.hostName);
//                errorMessage = "Unknown host: '" + formatHostString(connectionParams.hostName) + "'";
//            } catch (IOException ioe) {
//                logger.severe("ExecutionException.Couldn't connect to '" + connectionParams.hostName
//                        + ":" + connectionParams.getPortNumber() + "':\n" + ioe.getMessage());
//                logger.log(Level.FINEST, "Couldn't connect to '" + connectionParams.hostName
//                        + ":" + connectionParams.getPortNumber() + "':\n" + ioe.getMessage(), ioe);
//                errorMessage = "Couldn't connect to '" + formatHostString(connectionParams.hostName)
//                        + ":" + connectionParams.getPortNumber() + "':\n" + ioe.getMessage();
//            } catch (CancelConnectionQuietlyException cce) {
//                logger.warning("ExecutionException.Cancelled by user: " + cce.getMessage());
////                errorMessage = null; // exit without dialog showing
//            } catch (CancelConnectionException cce) {
//                logger.severe("ExecutionException.Cancelled: " + cce.getMessage());
//                errorMessage = cce.getMessage();
//            } catch (ConnectionErrorException cee) {
//                logger.severe("ExecutionException.ConnectionErrorException: " + cee.getMessage() + " host: "
//                        + connectionParams.hostName + ":" + connectionParams.getPortNumber());
//                errorMessage = cee.getMessage() + "\nHost: "
//                        + formatHostString(connectionParams.hostName) + ":" + connectionParams.getPortNumber();
//            } catch (Throwable throwable) {
//                logger.severe("ExecutionException.throwable" + throwable.getMessage());
//                logger.log(Level.FINEST, "Couldn't connect to '" + formatHostString(connectionParams.hostName)
//                        + ":" + connectionParams.getPortNumber() + "':\n" + throwable.getMessage(), throwable);
//                errorMessage = "Couldn't connect to '" + formatHostString(connectionParams.hostName)
//                        + ":" + connectionParams.getPortNumber() + "':\n" + throwable.getMessage();
//            }
//            if (errorMessage != null) {
//                logger.severe("errorMessage != null, presenting...");
//                presenter.showConnectionErrorDialog(errorMessage);
//                logger.severe("errorMessage != null, presented");
//            }
//            presenter.clearMessage();
//            presenter.connectionFailed();
//            logger.severe("end of bulk");
        }
        logger.info("creating socket done.end");
    }

    @Override
    public void setConnectionParams(ConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = (ConnectionPresenter) presenter;
    }

    @Override
    public void setHasSshSupport(boolean hasSshSupport) {
        this.hasSshSupport = hasSshSupport;
    }

    @Override
    public boolean cancel() {
        if (hasSshSupport && sshConnectionManager != null && sshConnectionManager.isConnected()) {
            sshConnectionManager.closeConnection();
            sshConnectionManager = null;
        }
        return super.cancel(true);
    }
}
