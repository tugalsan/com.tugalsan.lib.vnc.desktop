package com.tugalsan.api.desktop.server;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TS_DesktopDialogPasswordUtils {

    public static String show(String title) {
        return show(null, title);
    }

    public static String show(String title, int passwordLength) {
        return show(null, title, passwordLength);
    }

    public static String show(Component parent, String title) {
        var p = new TS_DesktopDialogPasswordGUI();
        return show(parent, p, title);
    }

    public static String show(Component parent, String title, int passwordLength) {
        var p = new TS_DesktopDialogPasswordGUI(passwordLength);
        return show(parent, p, title);
    }

    private static String show(Component parent, TS_DesktopDialogPasswordGUI panel, String title) {
        var pane = new JOptionPane(panel);
        pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        var dia = pane.createDialog(parent, title);
        dia.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                panel.gainedFocus();
            }
        });
        dia.setDefaultCloseOperation(JOptionPane.OK_OPTION); // necessary?
        dia.setVisible(true);
        var val = pane.getValue();
        if (val == null) {
            return null;
        }
        return val.equals(JOptionPane.OK_OPTION) ? panel.getPassword() : null;
    }
}
