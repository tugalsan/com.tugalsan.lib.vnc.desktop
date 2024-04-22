package com.tugalsan.api.desktop.server;

import java.net.*;
import javax.swing.*;

public class TS_DesktopResourceUtils {

    public static URL url(String loc) {
        return TS_DesktopResourceUtils.class.getResource(loc);
    }

    public static ImageIcon imageIcon(String loc) {
        var url = url(loc);
        return url == null ? null : new ImageIcon(url);
    }
}
