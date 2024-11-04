package com.tugalsan.lib.vnc.desktop.server;

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_ParametersHandler;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_Viewer;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_CliParser;
import java.awt.Window;
import javax.swing.JDesktopPane;

public class TS_LibVncDesktop {

    public static TS_LibVncDesktopViewer_Viewer show(TS_ThreadSyncTrigger killTrigger, JDesktopPane pane, Window window, TS_LibVncDesktopViewer_CliParser parser) {
        return new TS_LibVncDesktopViewer_Viewer(killTrigger, parser, pane, window);
    }

    public static void connect(TS_ThreadSyncTrigger killTrigger, JDesktopPane pane, Window window, TS_LibVncDesktopViewer_CliParser parser) {
        show(killTrigger, pane, window, parser).connectAction();
    }

    public static TS_LibVncDesktopViewer_Viewer show(TS_ThreadSyncTrigger killTrigger, JDesktopPane pane, Window window, boolean viewOnly, String ipNumber_orURL, String password) {
        var connParams = TS_LibVncDesktop.defaultParser(viewOnly);
        connParams.addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_HOST, ipNumber_orURL, "");
        connParams.addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_PASSWORD, password, "");
        return show(killTrigger, pane, window, connParams);
    }

    public static void connect(TS_ThreadSyncTrigger killTrigger, JDesktopPane pane, Window window, boolean viewOnly, String ipNumber_orURL, String password) {
        show(killTrigger, pane, window, viewOnly, ipNumber_orURL, password).connectAction();
    }

    private static TS_LibVncDesktopViewer_CliParser defaultParser(boolean viewOnly) {
        var vncHost = "vncHostEmptyInit";
        Integer vncPort_orNull = null; //default
        var vncPassword = "vncPasswordEmptyInit";
        var sshEnable = false;
        var sshHost = "sshHostEmptyInit";
        Integer sshPort_orNull = 5900; //default
        var sshUser = "sshUserEmptyInit";
        Integer compLevel_from_1_to_9_orNull_def6 = null; //default
        Integer imgQuality_from_1_to_9_orNull_def6 = null; //default
        Integer bitPerPixel_3_6_8_16_24_32_or_null_defServer = null; //default
//        if (sshEnable && !TS_NetworkPortUtils.isReacable(sshHost, sshPort_orNull, 5)) {
//            TS_DesktopDialogMessageUtils.show("SSH Host'a ulaşılamıyor hatası");
//            return null;
//        }
        return new TS_LibVncDesktopViewer_CliParser(vncHost, vncPort_orNull, vncPassword,
                sshEnable, sshHost, sshPort_orNull, sshUser,
                viewOnly, compLevel_from_1_to_9_orNull_def6, imgQuality_from_1_to_9_orNull_def6, bitPerPixel_3_6_8_16_24_32_or_null_defServer);
    }
}
