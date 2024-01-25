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
package com.tugalsan.lib.vnc.desktop.server.viewer.swing.ssh;

import com.tugalsan.lib.vnc.desktop.server.exceptions.AuthenticationFailedException;
import com.tugalsan.lib.vnc.desktop.server.utils.Strings;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.ConnectionParams;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.CancelConnectionQuietlyException;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ConnectionErrorException;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.RequestSomethingDialog;
import com.trilead.ssh2.*;
import com.trilead.ssh2.channel.LocalAcceptThread;
import com.trilead.ssh2.crypto.Base64;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author dime at tightvnc.com
 */
public class TrileadSsh2ConnectionManager extends SshConnectionManager {

    private static final Logger logger = Logger.getLogger(TrileadSsh2ConnectionManager.class.getName());

    private final Set<File> identityFiles = new HashSet<>();
    private LocalPortForwarder portForwarder;
    private boolean connected = false;
    private Connection connection;

    public TrileadSsh2ConnectionManager(JFrame parentWindow) {
        super(parentWindow);
    }

    @Override
    protected void initSshEngine() {
        //nop
    }

    @Override
    protected int makeConnectionAndGetPort(ConnectionParams connectionParams) throws ConnectionErrorException {
        connected = false;
        var port = 0;
        connection = new Connection(connectionParams.getSshHostName(), connectionParams.getSshPortNumber());
        try {
            var knownHosts = getKnownHosts();
            var connectionInfo = connection.connect(new HostVerifier(knownHosts));
            logger.log(Level.FINE, "SSH connection established:\n  clientToServerCryptoAlgorithm:{0}\n  clientToServerMACAlgorithm: {1}\n  keyExchangeAlgorithm: {2}\n  serverHostKeyAlgorithm: {3}\n  serverToClientCryptoAlgorithm: {4}\n  serverToClientMACAlgorithm: {5}", new Object[]{connectionInfo.clientToServerCryptoAlgorithm, connectionInfo.clientToServerMACAlgorithm, connectionInfo.keyExchangeAlgorithm, connectionInfo.serverHostKeyAlgorithm, connectionInfo.serverToClientCryptoAlgorithm, connectionInfo.serverToClientMACAlgorithm});

            if (!connection.isAuthenticationComplete()) {
                tryAuthenticate(connectionParams, connection);
            }
            if (!connection.isAuthenticationComplete()) {
                throw new ConnectionErrorException("No supported authentication methods available.");
            }
            connection.setTCPNoDelay(true);
            portForwarder = connection.createLocalPortForwarder(port, connectionParams.getHostName(), connectionParams.getPortNumber());
            port = getPortNumber(portForwarder);
            connected = true;
        } catch (CancelConnectionQuietlyException | AuthenticationFailedException e) {
            logger.fine(e.getMessage());
            errorMessage = e.getMessage();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Cannot establish SSH connection: " + e.getMessage(), e);
            errorMessage = e.getMessage();
        } finally {
            if (!connected) {
                closeConnection();
            }
        }
        if (!connected) {
            throw new ConnectionErrorException("Cannot establish SSH connection: " + errorMessage);
        }
        return port;
    }

    @Override
    public void closeConnection() {
        if (portForwarder != null) {
            try {
                portForwarder.close();
                portForwarder = null;
            } catch (IOException e) {
                logger.warning("There was a problem while closing ssh port forwarder: +s".formatted(e.getMessage()));
            }
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        logger.fine("Close ssh connection");
    }

    private void tryAuthenticate(ConnectionParams connectionParams, Connection connection) throws Throwable {
        var remainingAuthMethods = connection.getRemainingAuthMethods(connectionParams.getSshUserName());
        logger.finer("Supported auth methods: %s".formatted(Arrays.toString(remainingAuthMethods)));
        for (var authMethod : remainingAuthMethods) {// publickey, keyboard-interactive
            if ("publickey".equals(authMethod)) {

                for (var keyFile : identityFiles) {
                    logger.fine("Trying 'publickey' auth with %s".formatted(keyFile.toString()));
                    String passphrase = null;
                    String title;
                    String message;
                    if (isKeyFileEncrypted(keyFile)) {
                        if (keyFile.getName().contains("rsa")) {
                            title = "RSA Authentication";
                            message = "Enter RSA private key password:";
                        } else if (keyFile.getName().contains("dsa")) {
                            title = "DSA Authentication";
                            message = "Enter DSA private key password:";
                        } else {
                            title = "SSH Authentication";
                            message = "Enter private key password:";
                        }
                        passphrase = getPassphrase(title, message);
                    }
                    if (connection.authenticateWithPublicKey(connectionParams.getSshUserName(), keyFile, passphrase)) {
                        logger.info("Authenticated with %s".formatted(keyFile.getName()));
                        return;
                    }
                }
            }
            if ("keyboard-interactive".equals(authMethod)) {
                logger.fine("Trying 'keyboard-interactive' auth");
                try {
                    if (connection.authenticateWithKeyboardInteractive(connectionParams.getSshUserName(),
                            new InteractiveInputCallback())) {
                        return;
                    } else {
                        throw new AuthenticationFailedException("Authentication failed");
                    }
                } catch (IOException e) {
                    TGS_UnSafe.thrw(e);
                }
            }
            if ("password".equals(authMethod)) {
                logger.fine("Trying 'password' auth");
                if (connection.authenticateWithPassword(connectionParams.getSshUserName(),
                        getPassphrase("Password Authentication",
                                "Enter password for " + connectionParams.getSshUserName()))) {
                    return;
                } else {
                    throw new AuthenticationFailedException("Authentication failed");
                }
            }
        }
    }

    private KnownHosts getKnownHosts() throws IOException {
        var knownHosts = new KnownHosts();
        var sshNode = Preferences.userRoot().node(SSH_NODE);
        try {
            //                     code bellow converts byte array to char array
            knownHosts.addHostkeys(PrefsHelper.getStringFrom(sshNode, KNOWN_HOSTS).toCharArray());
        } catch (IOException e) {
            PrefsHelper.clearNode(sshNode);
        }
        var knownHostsFile = new File(OPENSSH_CONFIG_DIR_NAME, KNOWN_HOSTS);
        if (knownHostsFile.exists() && knownHostsFile.isFile()) {
            knownHosts.addHostkeys(knownHostsFile);
        }
        return knownHosts;
    }

    private String getPassphrase(String title, String message) {
        var dialog = new RequestSomethingDialog(parent, title, true, message);
        return dialog.askResult() ? dialog.getResult() : "";
    }

    /**
     * get local port by reflection because at this library fields we need have
     * got package local visibility
     */
    private int getPortNumber(LocalPortForwarder portForwarder) throws IOException {
        final Field latField;
        try {
            latField = portForwarder.getClass().getDeclaredField("lat");
            latField.setAccessible(true);
            var lat = (LocalAcceptThread) latField.get(portForwarder);
            var ssField = lat.getClass().getDeclaredField("ss");
            ssField.setAccessible(true);
            return ((ServerSocket) ssField.get(lat)).getLocalPort();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.throwing(this.getClass().getName(), "getPortNumber(..)", e);
            throw new IOException(e.getMessage());
        }
    }

    @Override
    protected void addIdentityFile(File keyFile) {
        identityFiles.add(keyFile);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * @author dime at tightvnc.com
     */
    private class InteractiveInputCallback implements InteractiveCallback {

        @Override
        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws Exception {
            var answers = new String[numPrompts];
            for (var i = 0; i < numPrompts; ++i) {
                var dialog = new RequestSomethingDialog(
                        parent,
                        "Keyboard Interactive Authentication", !echo[i],
                        Strings.isTrimmedEmpty(name) ? "SSH Authentication" : name,
                        null == instruction ? "" : instruction, prompt[i])
                        .setOkLabel("Login");
                if (dialog.askResult()) {
                    answers[i] = dialog.getResult();
                } else {
                    throw new CancelConnectionQuietlyException("Login interrupted by user");
                }
            }
            return answers;
        }
    }

    private class HostVerifier implements ServerHostKeyVerifier {

        private final KnownHosts knownHosts;

        HostVerifier(KnownHosts knownHosts) {
            this.knownHosts = knownHosts;
        }

        @Override
        public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
            var result = knownHosts.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);
            String message;
            switch (result) {
                case KnownHosts.HOSTKEY_IS_OK -> {
                    return true;
                }
                case KnownHosts.HOSTKEY_IS_NEW ->
                    message = "Do you want to accept the hostkey (type " + serverHostKeyAlgorithm + ") from " + hostname + " ?";
                case KnownHosts.HOSTKEY_HAS_CHANGED ->
                    message = "WARNING! Hostkey for " + hostname + " has changed!\nAccept anyway?";
                default ->
                    throw new IllegalStateException();
            }
            var hexFingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
            var bubblebabbleFingerprint = KnownHosts.createBubblebabbleFingerprint(serverHostKeyAlgorithm,
                    serverHostKey);
            message += "\n\nHex Fingerprint: " + hexFingerprint + "\nBubblebabble Fingerprint: " + bubblebabbleFingerprint;
            var yesNoDialog = new RequestYesNoDialog(parent, "SSH: Host Verification", message);
            var verified = yesNoDialog.ask();

            if (verified) {
                addHostkeyToStorages(hostname, serverHostKeyAlgorithm, serverHostKey);
            }
            return verified;
        }

        void addHostkeyToStorages(String hostname, String serverHostKeyAlgorithm, byte[] serverHostKey) throws IOException {
            var sshNode = Preferences.userRoot().node(SSH_NODE);
            var record = hostname + " " + serverHostKeyAlgorithm + " " + new String(Base64.encode(serverHostKey));
            PrefsHelper.addRecordTo(sshNode, KNOWN_HOSTS, record);

            KnownHosts.addHostkeyToFile(new File(OPENSSH_CONFIG_DIR_NAME, KNOWN_HOSTS),
                    new String[]{hostname}, serverHostKeyAlgorithm, serverHostKey);
        }
    }

}
