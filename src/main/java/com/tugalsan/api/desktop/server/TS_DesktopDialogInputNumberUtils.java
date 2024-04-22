package com.tugalsan.api.desktop.server;

import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import javax.swing.*;

public class TS_DesktopDialogInputNumberUtils {

    public static Optional<Integer> show(String title) {
        return show(null, title);
    }

    public static Optional<Integer> show(String title, Integer initValue) {
        return show(null, title, initValue);
    }

    public static Optional<Integer> show(Component parent, String title) {
        var p = new TS_DesktopDialogInputNumberGUI();
        return show(parent, p, title);
    }

    public static Optional<Integer> show(Component parent, String title, Integer initValue) {
        var p = new TS_DesktopDialogInputNumberGUI(initValue);
        return show(parent, p, title);
    }

    private static Optional<Integer> show(Component parent, TS_DesktopDialogInputNumberGUI panel, String title) {
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
        return val.equals(JOptionPane.OK_OPTION) ? panel.getNumber() : Optional.empty();
    }
}
