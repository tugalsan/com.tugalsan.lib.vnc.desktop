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

import com.tugalsan.api.thread.server.async.run.TS_ThreadAsyncRun;

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
public class TS_LibVncDesktopViewerSwing_GuiConnectionDialogView extends JPanel implements TS_LibVncDesktopViewer_MvpView, TS_LibVncDesktopViewerSwing_GuiConnectionView {

    private static final int PADDING = 4;
    private static final int COLUMNS_PORT_USER_FIELD = 13;
    private static final String CLOSE = "Close";
    private static final String CANCEL = "Cancel";
    private final TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener onCloseListener;
    public final JTextField serverPortField;
    public final JTextField serverNameField;
    private JButton connectButton;
    private final JInternalFrame view;
    private final TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter;
    private final StatusBar statusBar;
    private boolean connectionInProgress;
    private JButton closeCancelButton;
    private final TS_LibVncDesktopViewer_Viewer viewer;
    private final Window window;

    public TS_LibVncDesktopViewerSwing_GuiConnectionDialogView(final TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener onCloseListener, final TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter, JDesktopPane pane, Window window) {
        this.onCloseListener = onCloseListener;
        this.presenter = presenter;
        this.window = window;

        setLayout(new BorderLayout(0, 0));
        var optionsPane = new JPanel(new GridBagLayout());
        add(optionsPane, BorderLayout.CENTER);
        optionsPane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

        setLayout(new GridBagLayout());

        var gridRow = 0;

        viewer = (TS_LibVncDesktopViewer_Viewer) onCloseListener;
        serverNameField = new JTextField(viewer.connectionParams.hostName);
        initConnectionsHistoryCombo();

        addFormFieldRow(optionsPane, gridRow, new JLabel("Remote Host:"), serverNameField, true);
        ++gridRow;

        serverPortField = new JTextField(COLUMNS_PORT_USER_FIELD);

        addFormFieldRow(optionsPane, gridRow, new JLabel("Port:"), serverPortField, false);
        ++gridRow;

        var buttonPanel = createButtons();

        var cButtons = new GridBagConstraints();
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
        var prototypeDisplayValue = new TS_LibVncDesktopViewer_SettingsViewerConnectionParams();
        prototypeDisplayValue.hostName = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    }

    @Override
    public void showReconnectDialog(final String title, final String message) {
        var val = JOptionPane.showConfirmDialog(view,
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
        var buttonPanel = new JPanel();

        closeCancelButton = new JButton(CLOSE);
        closeCancelButton.addActionListener((ActionEvent e) -> {
            if (connectionInProgress) {
                presenter.cancelConnection();
                setConnectionInProgress(false);
            } else {
                closeView();
                closeApp();
            }
        });

        connectButton = new JButton("Connect");
        buttonPanel.add(connectButton);
        connectButton.addActionListener((ActionEvent e) -> {
            connectAction();
        });

        var optionsButton = new JButton("Options...");
        buttonPanel.add(optionsButton);
        optionsButton.addActionListener((ActionEvent e) -> {
            var od = new TS_LibVncDesktopViewerSwing_GuiOptionsDialog(window);
            od.initControlsFromSettings(presenter.getRfbSettings(), presenter.getUiSettings(), true);
            od.setVisible(true);
            view.toFront();
        });

        buttonPanel.add(closeCancelButton);
        return buttonPanel;
    }

    public void connectAction() {
        TS_ThreadAsyncRun.now(viewer.killTrigger, kt -> {
            setMessage("");
            var hostName = serverNameField.getText();
            try {
                setConnectionInProgress(true);
                presenter.submitConnection(hostName);
            } catch (TS_LibVncDesktopViewer_SettingsWrongParameterException wpe) {
                var wpe_prp = wpe.getPropertyName();
                var wpe_msg = wpe.getMessage();
                if (TS_LibVncDesktopViewerSwing_ConnectionPresenter.PROPERTY_HOST_NAME.equals(wpe_prp)) {
                    serverNameField.requestFocusInWindow();
                }
                if (TS_LibVncDesktopViewerSwing_ConnectionPresenter.PROPERTY_RFB_PORT_NUMBER.equals(wpe_prp)) {
                    serverPortField.requestFocusInWindow();
                }
                showConnectionErrorDialog(wpe_msg);
                setConnectionInProgress(false);
            }
        });
    }

    private void addFormFieldRow(JPanel pane, int gridRow, JLabel label, JComponent field, boolean fill) {
        var cLabel = new GridBagConstraints();
        cLabel.gridx = 0;
        cLabel.gridy = gridRow;
        cLabel.weightx = 0;
        cLabel.weighty = 100;
        cLabel.gridwidth = cLabel.gridheight = 1;
        cLabel.anchor = GridBagConstraints.LINE_END;
        cLabel.ipadx = PADDING;
        cLabel.ipady = 10;
        pane.add(label, cLabel);

        var cField = new GridBagConstraints();
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
    public void showConnectionErrorDialog(String message) {
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
        var f = messageLabel.getFont();
        messageLabel.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        add(messageLabel, BorderLayout.CENTER);

        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        add(rightPanel, BorderLayout.EAST);
        setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                var oldColor = g.getColor();
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
