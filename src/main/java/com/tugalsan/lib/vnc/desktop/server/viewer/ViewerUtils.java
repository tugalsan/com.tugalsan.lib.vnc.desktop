package com.tugalsan.lib.vnc.desktop.server.viewer;

import com.tugalsan.lib.vnc.desktop.server.viewer.cli.Parser;
import java.awt.Window;
import javax.swing.JDesktopPane;

public class ViewerUtils {

    public static Viewer show(JDesktopPane pane, Window window, Parser parser) {
        return new Viewer(parser, pane, window);
    }

    public static void connect(JDesktopPane pane, Window window, Parser parser) {
        show(pane, window, parser).connectAction();
    }

    public static Viewer show(JDesktopPane pane, Window window, boolean viewOnly, String ipNumber_orURL, String password) {
        var connParams = ViewerUtils.defaultParser(viewOnly);
        connParams.addOption(ParametersHandler.ARG_HOST, ipNumber_orURL, "");
        connParams.addOption(ParametersHandler.ARG_PASSWORD, password, "");
        return show(pane, window, connParams);
    }

    public static void connect(JDesktopPane pane, Window window, boolean viewOnly, String ipNumber_orURL, String password) {
        show(pane, window, viewOnly, ipNumber_orURL, password).connectAction();
    }

    public static Parser defaultParser(boolean viewOnly) {
        String vncHost = "vncHostEmptyInit";
        Integer vncPort_orNull = null; //default
        String vncPassword = "vncPasswordEmptyInit";
        boolean sshEnable = false;
        String sshHost = "sshHostEmptyInit";
        Integer sshPort_orNull = 5900; //default
        String sshUser = "sshUserEmptyInit";
        Integer compLevel_from_1_to_9_orNull_def6 = null; //default
        Integer imgQuality_from_1_to_9_orNull_def6 = null; //default
        Integer bitPerPixel_3_6_8_16_24_32_or_null_defServer = null; //default
//        if (sshEnable && !TS_NetworkPortUtils.isReacable(sshHost, sshPort_orNull, 5)) {
//            TS_DesktopDialogMessageUtils.show("SSH Host'a ulaşılamıyor hatası");
//            return null;
//        }
        return new Parser(vncHost, vncPort_orNull, vncPassword,
                sshEnable, sshHost, sshPort_orNull, sshUser,
                viewOnly, compLevel_from_1_to_9_orNull_def6, imgQuality_from_1_to_9_orNull_def6, bitPerPixel_3_6_8_16_24_32_or_null_defServer);
    }
}
