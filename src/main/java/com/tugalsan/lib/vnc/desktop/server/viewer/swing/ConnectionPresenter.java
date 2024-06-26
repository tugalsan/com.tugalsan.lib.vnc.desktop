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

import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.ProtocolSettings;
import com.tugalsan.lib.vnc.desktop.server.utils.Strings;
import com.tugalsan.lib.vnc.desktop.server.utils.ViewerControlApi;
import com.tugalsan.lib.vnc.desktop.server.viewer.Viewer;
import com.tugalsan.lib.vnc.desktop.server.viewer.mvp.Presenter;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.ConnectionParams;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.UiSettings;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.WrongParameterException;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.ConnectionView;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.AbstractConnectionWorkerFactory;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.NetworkConnectionWorker;
import com.tugalsan.lib.vnc.desktop.server.viewer.workers.RfbConnectionWorker;

import java.net.Socket;
import java.util.logging.Logger;

/**
 * A Presenter (controller) that presents business logic for connection
 * establishing interactions
 *
 * Before starting connection process you have to add View(s) and Model(s), and
 * need to set @see ConnectionWorkerFactory
 *
 * @author dime at tightvnc.com
 */
public class ConnectionPresenter extends Presenter {

    public static final String PROPERTY_HOST_NAME = "HostName";
    public static final String PROPERTY_RFB_PORT_NUMBER = "PortNumber";
    public static final String PROPERTY_USE_SSH = "UseSsh";
    private static final String PROPERTY_SSH_USER_NAME = "SshUserName";
    private static final String PROPERTY_SSH_HOST_NAME = "SshHostName";
    private static final String PROPERTY_SSH_PORT_NUMBER = "SshPortNumber";
    private static final String PROPERTY_STATUS_BAR_MESSAGE = "Message";
    private static final String PROPERTY_CONNECTION_IN_PROGRESS = "ConnectionInProgress";
    public static final String CONNECTION_PARAMS_MODEL = "ConnectionParamsModel";
    public static final String CONNECTION_VIEW = "ConnectionView";

    private ProtocolSettings rfbSettings;
    private UiSettings uiSettings;
    private static final Logger logger = Logger.getLogger(ConnectionPresenter.class.getName());
    private RfbConnectionWorker rfbConnectionWorker;
    private AbstractConnectionWorkerFactory connectionWorkerFactory;
    private NetworkConnectionWorker networkConnectionWorker;
    private boolean needReconnection = true;
    private ViewerControlApi viewerControlApi;

    public ConnectionPresenter(Viewer viewer) {
        super(viewer);
    }

    public void startConnection(ProtocolSettings rfbSettings, UiSettings uiSettings)
            throws IllegalStateException {
        startConnection(rfbSettings, uiSettings, 0);
    }

    public void startConnection(ProtocolSettings rfbSettings, UiSettings uiSettings, int paramSettingsMask)
            throws IllegalStateException {
        this.rfbSettings = rfbSettings;
        this.uiSettings = uiSettings;
        if (!isModelRegisteredByName(CONNECTION_PARAMS_MODEL)) {
            throw new IllegalStateException("No Connection Params model added.");
        }
        syncModels(paramSettingsMask);
        show();
        populate();
        if (!uiSettings.showConnectionDialog) {
            connect();
        }
//        viewer.main.tiltWindows();
    }

    public void setUseSsh(boolean useSsh) {
        setModelProperty(PROPERTY_USE_SSH, useSsh, boolean.class);
    }

    /**
     * Initiate connection process from View ex. by press "Connect" button
     *
     * @param hostName name of host to connect
     */
    public void submitConnection(String hostName) throws WrongParameterException {
        if (Strings.isTrimmedEmpty(hostName)) {
            throw new WrongParameterException("Host name is empty", PROPERTY_HOST_NAME);
        }
        setModelProperty(PROPERTY_HOST_NAME, hostName);

        var rfbPort = (String) getViewPropertyOrNull(PROPERTY_RFB_PORT_NUMBER);
        setModelProperty(PROPERTY_RFB_PORT_NUMBER, rfbPort);
        try {
            throwPossiblyHappenedException();
        } catch (Throwable e) {
            throw new WrongParameterException("Wrong Port", PROPERTY_HOST_NAME);
        }
        connect();
    }

    /**
     * Prepares async network connection worker and starts to execute it Network
     * connection worker tries to establish tcp connection with remote host
     */
    public void connect() {
        var connectionParams = (ConnectionParams) getModel(CONNECTION_PARAMS_MODEL);
        if (null == connectionWorkerFactory) {
            throw new IllegalStateException("connectionWorkerFactory is not set");
        }
        networkConnectionWorker = connectionWorkerFactory.createNetworkConnectionWorker();
        networkConnectionWorker.setConnectionParams(connectionParams);
        networkConnectionWorker.setPresenter(this);
        networkConnectionWorker.run();
    }

    /**
     * Callback for connection worker, invoked on failed connection attempt.
     * Both for tcp network connection worker and for rfb connection worker.
     */
    void connectionFailed() {
        cancelConnection();
        reconnect(null);
    }

    /**
     * Callback for connection worker, invoked when connection is cancelled
     */
    void connectionCancelled() {
        cancelConnection();
        enableConnectionDialog();
    }

    private void enableConnectionDialog() {
        setViewProperty(PROPERTY_CONNECTION_IN_PROGRESS, false, boolean.class);
    }

    /**
     * Callback for tcp network connection worker. Invoked on successful
     * connection. Invoked in EDT
     *
     * @param workingSocket a socket binded with established connection
     */
    void successfulNetworkConnection(Socket workingSocket) { // EDT
        logger.info("Connected");
        showMessage("Connected");
        rfbConnectionWorker = connectionWorkerFactory.createRfbConnectionWorker();
        rfbConnectionWorker.setWorkingSocket(workingSocket);
        rfbConnectionWorker.setRfbSettings(rfbSettings);
        rfbConnectionWorker.setUiSettings(uiSettings);
        rfbConnectionWorker.setConnectionString(
                getModelProperty(PROPERTY_HOST_NAME) + ":" + getModelProperty(PROPERTY_RFB_PORT_NUMBER));
        rfbConnectionWorker.run();
        viewerControlApi = rfbConnectionWorker.getViewerControlApi();
    }

    /**
     * Callback for rfb connection worker Invoked on successful connection
     */
    void successfulRfbConnection() {
        enableConnectionDialog();
        getView(CONNECTION_VIEW).closeView();
    }

    /**
     * Gracefully cancel active connection workers
     */
    public void cancelConnection() {
        logger.finer("Cancel connection");
        if (networkConnectionWorker != null) {
            networkConnectionWorker.cancel();
        }
        if (rfbConnectionWorker != null) {
            rfbConnectionWorker.cancel();
        }
    }

    /**
     * Ask ConnectionView to show dialog whether to reconnect or close app
     *
     * @param errorTitle dialog title to show
     * @param errorMessage message to show
     */
    void showReconnectDialog(String errorTitle, String errorMessage) {
        var connectionView = (ConnectionView) getView(CONNECTION_VIEW);
        if (connectionView != null) {
            connectionView.showReconnectDialog(errorTitle, errorMessage);
        }
    }

    private void syncModels(int paramSettingsMask) {
//        final ConnectionParams cp = (ConnectionParams) getModel(CONNECTION_PARAMS_MODEL);
//        final ConnectionParams mostSuitableConnection = connectionsHistory.getMostSuitableConnection(cp);
//        cp.completeEmptyFieldsFrom(mostSuitableConnection);
//        rfbSettings.copyDataFrom(connectionsHistory.getProtocolSettings(mostSuitableConnection), paramSettingsMask & 0xffff);
//        uiSettings.copyDataFrom(connectionsHistory.getUiSettingsData(mostSuitableConnection), (paramSettingsMask >> 16) & 0xffff);
//        if (!cp.isHostNameEmpty()) {
//            connectionsHistory.reorder(cp, rfbSettings, uiSettings);
//        }

//        protocolSettings.addListener(connectionsHistory);
//        uiSettings.addListener(connectionsHistory);
    }

    /**
     * Show status info about currently executed operation
     *
     * @param message status message
     */
    void showMessage(String message) {
        setViewProperty(PROPERTY_STATUS_BAR_MESSAGE, message);
    }

    /**
     * Show empty status bar
     */
    void clearMessage() {
        showMessage("");
    }

    /**
     * Set connection worker factory
     *
     * @param connectionWorkerFactory factory
     */
    public void setConnectionWorkerFactory(AbstractConnectionWorkerFactory connectionWorkerFactory) {
        this.connectionWorkerFactory = connectionWorkerFactory;
    }

    /**
     * Reset presenter and try to start new connection establishing process
     *
     * @param predefinedPassword password
     */
    void reconnect(String predefinedPassword) {
        if (predefinedPassword != null && !predefinedPassword.isEmpty()) {
            connectionWorkerFactory.setPredefinedPassword(predefinedPassword);
        }
        clearMessage();
        enableConnectionDialog();
        show();
        populate();
        if (!uiSettings.showConnectionDialog) {
            connect();
        }
    }

    void clearPredefinedPassword() {
        connectionWorkerFactory.setPredefinedPassword(null);
    }

    public UiSettings getUiSettings() {
        return uiSettings;
    }

    public ProtocolSettings getRfbSettings() {
        return rfbSettings;
    }

    boolean needReconnection() {
        return needReconnection;
    }

    public void setNeedReconnection(boolean need) {
        needReconnection = need;
    }

    public ViewerControlApi getViewerControlApi() {
        return viewerControlApi;
    }
}
