package com.tugalsan.lib.vnc.desktop.server;

import module java.desktop;
import module com.tugalsan.api.thread;
import com.tugalsan.lib.vnc.desktop.server.viewer.*;

public class TS_LibVncDesktop {

    private static void show(TS_ThreadSyncTrigger killTrigger, boolean connect, JDesktopPane pane, Window window, TS_LibVncDesktopViewer_CliParser parser) {
        var v = new TS_LibVncDesktopViewer_Viewer(killTrigger, parser, pane, window);
        if (connect) {
            v.connectAction();
        }
    }

    public static void show(TS_ThreadSyncTrigger killTrigger, boolean connect, JDesktopPane pane, Window window, boolean viewOnly, String ipNumber_orURL, String password) {
        var connParams = TS_LibVncDesktop.defaultParser(viewOnly);
        connParams.addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_HOST, ipNumber_orURL, "");
        connParams.addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_PASSWORD, password, "");
        show(killTrigger, connect, pane, window, connParams);
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
