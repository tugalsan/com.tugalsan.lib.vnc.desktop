package com.tugalsan.api.desktop.server;

import java.awt.*;
import javax.swing.*;

public class TS_DesktopDialogPasswordGUI extends JPanel {

    final private JPasswordField tf;
    final private JLabel lbl;
    private boolean focusedAlready;

    public void gainedFocus() {
        if (!focusedAlready) {
            focusedAlready = true;
            tf.requestFocusInWindow();
        }
    }

    public TS_DesktopDialogPasswordGUI(int length) {
        super(new FlowLayout());
        focusedAlready = false;
        tf = new JPasswordField(length);
        var d = new Dimension();
        d.setSize(30, 22);
        tf.setMinimumSize(d);
        tf.setColumns(10);
        lbl = new JLabel("Password: ");
        add(lbl);
        add(tf);
    }

    public TS_DesktopDialogPasswordGUI() {
        this(0);
    }

    public String getPassword() {
        return new String(tf.getPassword());
    }
}
