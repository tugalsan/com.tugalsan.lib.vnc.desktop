package com.tugalsan.api.desktop.server;

import java.util.Arrays;
import javax.swing.JMenuBar;

public class TS_DesktopJMenuButtonBar extends JMenuBar {

    private TS_DesktopJMenuButtonBar(TS_DesktopJMenuButton... buttons) {
        Arrays.stream(buttons).forEachOrdered(btn -> add(btn));
    }

    public static TS_DesktopJMenuButtonBar of(TS_DesktopJMenuButton... buttons) {
        return new TS_DesktopJMenuButtonBar(buttons);
    }
}
