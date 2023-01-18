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
package com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui;

import com.tugalsan.lib.vnc.desktop.server.viewer.Viewer;
import com.tugalsan.lib.vnc.desktop.server.viewer.mvp.View;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.ConnectionParams;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.WrongParameterException;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ConnectionPresenter;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ViewerEventsListener;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.ssh.SshConnectionManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Dialog window for connection parameters get from.
 */
@SuppressWarnings("serial")
public class ConnectionDialogView extends JPanel implements View, ConnectionView {

    private static final int PADDING = 4;
    private static final int COLUMNS_HOST_FIELD = 30;
    private static final int COLUMNS_PORT_USER_FIELD = 13;
    private static final String CLOSE = "Close";
    private static final String CANCEL = "Cancel";
    private ViewerEventsListener onCloseListener;
    private final boolean hasSshSupport;
    public final JTextField serverPortField;
    public JCheckBox useSshTunnelingCheckbox;
    public final JTextField serverNameField;
    public JTextField sshUserField;
    public JTextField sshHostField;
    public JTextField sshPortField;
    private JLabel sshUserLabel;
    private JLabel sshHostLabel;
    private JLabel sshPortLabel;
    private JLabel ssUserWarningLabel;
    private JButton connectButton;
    private final JInternalFrame view;
    private final ConnectionPresenter presenter;
    private final StatusBar statusBar;
    private boolean connectionInProgress;
    private JButton closeCancelButton;
    private Viewer viewer;
    private Window window;

    public ConnectionDialogView(final ViewerEventsListener onCloseListener, final ConnectionPresenter presenter, JDesktopPane pane, Window window) {
        this.onCloseListener = onCloseListener;
        this.hasSshSupport = SshConnectionManager.checkForSshSupport();
        this.presenter = presenter;
        this.window = window;

        setLayout(new BorderLayout(0, 0));
        JPanel optionsPane = new JPanel(new GridBagLayout());
        add(optionsPane, BorderLayout.CENTER);
        optionsPane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

        setLayout(new GridBagLayout());

        int gridRow = 0;

        viewer = (Viewer) onCloseListener;
        serverNameField = new JTextField(viewer.connectionParams.hostName);
        initConnectionsHistoryCombo();

        addFormFieldRow(optionsPane, gridRow, new JLabel("Remote Host:"), serverNameField, true);
        ++gridRow;

        serverPortField = new JTextField(COLUMNS_PORT_USER_FIELD);

        addFormFieldRow(optionsPane, gridRow, new JLabel("Port:"), serverPortField, false);
        ++gridRow;

        if (this.hasSshSupport) {
            gridRow = createSshOptions(optionsPane, gridRow);
        }

        JPanel buttonPanel = createButtons();

        GridBagConstraints cButtons = new GridBagConstraints();
        cButtons.gridx = 0;
        cButtons.gridy = gridRow;
        cButtons.weightx = 100;
        cButtons.weighty = 100;
        cButtons.gridwidth = 2;
        cButtons.gridheight = 1;
        optionsPane.add(buttonPanel, cButtons);

        view = new JInternalFrame("New TightVNC Connection");
        pane.add(view);
        view.add(this, BorderLayout.CENTER);
        statusBar = new StatusBar();
        view.add(statusBar, BorderLayout.SOUTH);

        view.getRootPane().setDefaultButton(connectButton);
        view.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                super.internalFrameClosing(e);
                onCloseListener.onViewerComponentClosing();
            }
        });
        view.setResizable(true);
        view.setLocation(0, 0);
    }

    private void initConnectionsHistoryCombo() {
        ConnectionParams prototypeDisplayValue = new ConnectionParams();
        prototypeDisplayValue.hostName = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    }

    @Override
    public void showReconnectDialog(final String title, final String message) {
        int val = JOptionPane.showConfirmDialog(view,
                message + "\nTry another connection?",
                title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (JOptionPane.NO_OPTION == val) {
            presenter.setNeedReconnection(false);
            closeView();
            view.dispose();
            closeApp();
        } else {
            // TODO return when allowInteractive, close window otherwise
//                forceConnectionDialog = allowInteractive;
        }
    }

    public void setConnectionInProgress(boolean enable) {
        if (enable) {
            connectionInProgress = true;
            closeCancelButton.setText(CANCEL);
            connectButton.setEnabled(false);
        } else {
            connectionInProgress = false;
            closeCancelButton.setText(CLOSE);
            connectButton.setEnabled(true);
        }
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel();

        closeCancelButton = new JButton(CLOSE);
        closeCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectionInProgress) {
                    presenter.cancelConnection();
                    setConnectionInProgress(false);
                } else {
                    closeView();
                    closeApp();
                }
            }
        });

        connectButton = new JButton("Connect");
        buttonPanel.add(connectButton);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectAction();
            }
        });

        JButton optionsButton = new JButton("Options...");
        buttonPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDialog od = new OptionsDialog(window);
                od.initControlsFromSettings(presenter.getRfbSettings(), presenter.getUiSettings(), true);
                od.setVisible(true);
                view.toFront();
            }
        });

        buttonPanel.add(closeCancelButton);
        return buttonPanel;
    }

    public void connectAction() {
        setMessage("");
        String hostName = serverNameField.getText();
        try {
            setConnectionInProgress(true);
            presenter.submitConnection(hostName);
        } catch (WrongParameterException wpe) {
            if (ConnectionPresenter.PROPERTY_HOST_NAME.equals(wpe.getPropertyName())) {
                serverNameField.requestFocusInWindow();
            }
            if (ConnectionPresenter.PROPERTY_RFB_PORT_NUMBER.equals(wpe.getPropertyName())) {
                serverPortField.requestFocusInWindow();
            }
            showConnectionErrorDialog(wpe.getMessage());
            setConnectionInProgress(false);
        }
    }

    private int createSshOptions(JPanel pane, int gridRow) {
        GridBagConstraints cUseSshTunnelLabel = new GridBagConstraints();
        cUseSshTunnelLabel.gridx = 0;
        cUseSshTunnelLabel.gridy = gridRow;
        cUseSshTunnelLabel.weightx = 100;
        cUseSshTunnelLabel.weighty = 100;
        cUseSshTunnelLabel.gridwidth = 2;
        cUseSshTunnelLabel.gridheight = 1;
        cUseSshTunnelLabel.anchor = GridBagConstraints.LINE_START;
        cUseSshTunnelLabel.ipadx = PADDING;
        cUseSshTunnelLabel.ipady = 10;
        useSshTunnelingCheckbox = new JCheckBox("Use SSH tunneling");
        pane.add(useSshTunnelingCheckbox, cUseSshTunnelLabel);
        ++gridRow;

        sshHostLabel = new JLabel("SSH Server:");
        sshHostField = new JTextField(COLUMNS_HOST_FIELD);
        addFormFieldRow(pane, gridRow, sshHostLabel, sshHostField, true);
        ++gridRow;

        sshPortLabel = new JLabel("SSH Port:");
        sshPortField = new JTextField(COLUMNS_PORT_USER_FIELD);
        addFormFieldRow(pane, gridRow, sshPortLabel, sshPortField, false);
        ++gridRow;

        sshUserLabel = new JLabel("SSH User:");
        sshUserField = new JTextField(COLUMNS_PORT_USER_FIELD);
        JPanel sshUserFieldPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sshUserFieldPane.add(sshUserField);
        ssUserWarningLabel = new JLabel(" (will be asked if not specified)");
        sshUserFieldPane.add(ssUserWarningLabel);
        addFormFieldRow(pane, gridRow, sshUserLabel, sshUserFieldPane, false);
        ++gridRow;

        useSshTunnelingCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final boolean useSsh = e.getStateChange() == ItemEvent.SELECTED;
                setUseSsh(useSsh);
                presenter.setUseSsh(useSsh);
            }
        });
        presenter.setUseSsh(viewer.connectionParams.getUseSsh());
        return gridRow;
    }

    private void addFormFieldRow(JPanel pane, int gridRow, JLabel label, JComponent field, boolean fill) {
        GridBagConstraints cLabel = new GridBagConstraints();
        cLabel.gridx = 0;
        cLabel.gridy = gridRow;
        cLabel.weightx = 0;
        cLabel.weighty = 100;
        cLabel.gridwidth = cLabel.gridheight = 1;
        cLabel.anchor = GridBagConstraints.LINE_END;
        cLabel.ipadx = PADDING;
        cLabel.ipady = 10;
        pane.add(label, cLabel);

        GridBagConstraints cField = new GridBagConstraints();
        cField.gridx = 1;
        cField.gridy = gridRow;
        cField.weightx = 0;
        cField.weighty = 100;
        cField.gridwidth = cField.gridheight = 1;
        cField.anchor = GridBagConstraints.LINE_START;
        if (fill) {
            cField.fill = GridBagConstraints.HORIZONTAL;
        }
        pane.add(field, cField);
    }

    /*
     * Implicit View interface
     */
    public void setMessage(String message) {
        statusBar.setMessage(message);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPortNumber(int portNumber) {
        serverPortField.setText(String.valueOf(portNumber));
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getPortNumber() {
        return serverPortField.getText();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSshHostName(String sshHostName) {
        if (hasSshSupport) {
            sshHostField.setText(sshHostName);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getSshHostName() {
        if (hasSshSupport) {
            return sshHostField.getText();
        } else {
            return "";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSshPortNumber(int sshPortNumber) {
        if (hasSshSupport) {
            sshPortField.setText(String.valueOf(sshPortNumber));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getSshPortNumber() {
        if (hasSshSupport) {
            return sshPortField.getText();
        } else {
            return "";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSshUserName(String sshUserName) {
        if (hasSshSupport) {
            sshUserField.setText(sshUserName);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getSshUserName() {
        if (hasSshSupport) {
            return sshUserField.getText();
        } else {
            return "";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setUseSsh(boolean useSsh) {
        if (hasSshSupport) {
            useSshTunnelingCheckbox.setSelected(useSsh);
            sshUserLabel.setEnabled(useSsh);
            sshUserField.setEnabled(useSsh);
            ssUserWarningLabel.setEnabled(useSsh);
            sshHostLabel.setEnabled(useSsh);
            sshHostField.setEnabled(useSsh);
            sshPortLabel.setEnabled(useSsh);
            sshPortField.setEnabled(useSsh);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean getUseSsh() {
        return useSshTunnelingCheckbox.isSelected();
    }

    /*
     * /Implicit View interface
     */
    @Override
    public void showView() {
        view.setVisible(true);
        view.toFront();
        view.repaint();
    }

    @Override
    public void closeView() {
        view.setVisible(false);
    }

    @Override
    public void showConnectionErrorDialog(final String message) {
        JOptionPane.showMessageDialog(view, message, "Connection error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void closeApp() {
        if (onCloseListener != null) {
            onCloseListener.onViewerComponentClosing();
        }
    }

    @Override
    public JInternalFrame getFrame() {
        return view;
    }

}

class StatusBar extends JPanel {

    private JLabel messageLabel;

    StatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 23));

        messageLabel = new JLabel("");
        final Font f = messageLabel.getFont();
        messageLabel.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        add(messageLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        add(rightPanel, BorderLayout.EAST);
        setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Color oldColor = g.getColor();
                g.translate(x, y);
                g.setColor(c.getBackground().darker());
                g.drawLine(0, 0, width - 1, 0);
                g.setColor(c.getBackground().brighter());
                g.drawLine(0, 1, width - 1, 1);
                g.translate(-x, -y);
                g.setColor(oldColor);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(2, 2, 2, 2);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
