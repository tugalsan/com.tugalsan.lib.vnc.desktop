package com.tugalsan.api.desktop.server;

import com.tugalsan.api.callable.client.TGS_Callable;
import com.tugalsan.api.runnable.client.TGS_Runnable;
import java.awt.Component;
import javax.swing.SwingUtilities;

public class TS_DesktopMainUtils {

    public static void setThemeAndinvokeLaterAndFixTheme(TGS_Callable<Component> component) {
        TS_DesktopThemeUtils.setTheme();
        TS_DesktopMainUtils.invokeLater(() -> {
            TS_DesktopThemeUtils.setThemeDarkLAF(component.call());
        });
    }

    public static void invokeLater(TGS_Runnable run) {
        SwingUtilities.invokeLater(() -> run.run());
    }
}
