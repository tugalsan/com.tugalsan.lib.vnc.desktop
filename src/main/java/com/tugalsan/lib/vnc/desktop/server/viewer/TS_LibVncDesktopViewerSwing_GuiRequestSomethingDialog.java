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

import com.tugalsan.api.desktop.server.TS_DesktopWindowAndFrameUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog extends JDialog {

    private static final int DEFAULT_INPUT_FIELD_LENGTH = 20;
    private static final String OK = "Ok";
    private static final String CANCEL = "Cancel";
    private static final int PAD = 8;
    private String answer = "";
    private Boolean result = false;
    private String okLabel;
    private String cancelLabel;
    private int inputFieldLength = DEFAULT_INPUT_FIELD_LENGTH;

    public TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog(Component parent, String title, final boolean isPassword, String... messages) {
        super((Window) parent, title, ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//        final WindowAdapter onClose = new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                setVisible(false);
//                answer = "";
//            }
//        };
//        addWindowListener(onClose);
        final JTextField inputField;
        if (isPassword) {
            inputField = new JPasswordField(inputFieldLength);
        } else {
            inputField = new JTextField(inputFieldLength);
        }
        var outerPane = new JPanel(new BorderLayout(PAD, PAD));
        outerPane.setBorder(new EmptyBorder(PAD, 2 * PAD, 2 * PAD, 2 * PAD));
//        final java.util.List<Image> applicationIcons = TS_WindowIconUtils.getApplicationIcons();
//        if (!applicationIcons.isEmpty()) {
//            final JLabel iconLabel = new JLabel(
//                    new ImageIcon(applicationIcons.get(applicationIcons.size() - 1).getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
//            outerPane.add(iconLabel, BorderLayout.WEST);
//            iconLabel.setBorder(new EmptyBorder(PAD, 2 * PAD, PAD, 2 * PAD));
//        }
        var listPane = new JPanel();
        outerPane.add(listPane, BorderLayout.CENTER);
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        listPane.add(Box.createVerticalStrut(PAD));
        if (messages.length > 0) {
            for (var i = 0; i < (messages.length - 1); ++i) {
                var label = new JLabel(messages[i]);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPane.add(label);
                listPane.add(Box.createVerticalStrut(PAD));
            }
            var last = messages[messages.length - 1];
            if (last.endsWith(":")) {
                var inputPanel = new JPanel();
                inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
                var label = new JLabel(last);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                inputPanel.add(label);
                inputPanel.add(inputField);
                inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPane.add(inputPanel);
            } else {
                inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPane.add(inputField);
            }
        } else {
            inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPane.add(inputField);
        }
        listPane.add(Box.createVerticalStrut(2 * PAD));

        if (null == okLabel) {
            okLabel = OK;
        }
        if (null == cancelLabel) {
            cancelLabel = CANCEL;
        }
        var buttonsPane = new JPanel();
        buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.LINE_AXIS));
        var okButton = new JButton(new AbstractAction(okLabel) {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = true;
                answer = isPassword ? new String(((JPasswordField) inputField).getPassword()) : inputField.getText();
                TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog.this.dispatchEvent(new WindowEvent(
                        TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        var cancelButton = new JButton(new AbstractAction(cancelLabel) {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = false;
                answer = "";
                TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog.this.setVisible(false);
                TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog.this.dispatchEvent(new WindowEvent(
                        TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        buttonsPane.add(Box.createHorizontalGlue());
        buttonsPane.add(cancelButton);
        buttonsPane.add(Box.createHorizontalStrut(PAD));
        buttonsPane.add(okButton);
        outerPane.add(buttonsPane, BorderLayout.SOUTH);

        add(outerPane);
        getRootPane().setDefaultButton(okButton);
        if (!inputField.requestFocusInWindow()) {
            inputField.requestFocus();
        }
        pack();
        TS_DesktopWindowAndFrameUtils.decorate(this);
        TS_DesktopWindowAndFrameUtils.center(this);
    }

    public TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog setOkLabel(String okLabel) {
        this.okLabel = okLabel;
        return this;
    }

    public TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog setCancelLabel(String cancelLabel) {
        this.cancelLabel = cancelLabel;
        return this;
    }

    public TS_LibVncDesktopViewerSwing_GuiRequestSomethingDialog setInputFieldLength(int inputFieldLength) {
        this.inputFieldLength = inputFieldLength;
        return this;
    }

    public boolean askResult() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            Logger.getLogger(this.getClass().getName()).severe(e.getMessage());
        }
        return result;
    }

    public String getResult() {
        return answer;
    }
}
