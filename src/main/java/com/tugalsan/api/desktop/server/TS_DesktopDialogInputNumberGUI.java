package com.tugalsan.api.desktop.server;

import com.tugalsan.api.cast.client.TGS_CastUtils;
import java.awt.*;
import java.util.Optional;
import javax.swing.*;

public class TS_DesktopDialogInputNumberGUI extends JPanel {

    final private JTextField tf = new JTextField();
    final private JLabel lbl = new JLabel();
    private boolean focusedAlready;

    public void gainedFocus() {
        if (!focusedAlready) {
            focusedAlready = true;
            tf.requestFocusInWindow();
        }
    }

    public TS_DesktopDialogInputNumberGUI(Integer initValue) {
        super(new FlowLayout());
        focusedAlready = false;
        var d = new Dimension();
        d.setSize(30, 22);
        tf.setMinimumSize(d);
        tf.setColumns(10);
        lbl.setText("#: ");
        if (initValue != null) {
            tf.setText(initValue.toString());
        }
        add(lbl);
        add(tf);
    }

    public TS_DesktopDialogInputNumberGUI() {
        this(null);
    }

    public Optional<Integer> getNumber() {
        var val = TGS_CastUtils.toInteger(tf.getText());
        return val == null ? Optional.empty() : Optional.of(val);
    }
}
