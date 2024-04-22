package com.tugalsan.api.log.server;

//import org.fusesource.jansi.Ansi.Color;
//import org.fusesource.jansi.*;

public class TS_LogUtils {
//
//    public static void setColoredConsole(boolean enableColoredCMD) {
//        TS_LogUtils.enableColoredCMD = enableColoredCMD;
//        if (enableColoredCMD) {
//            AnsiConsole.systemInstall();
//            reset();
//            result(TS_LogUtils.class.getSimpleName() + ".initialize->enableColoredCMD:true");
//        } else {
//            result(TS_LogUtils.class.getSimpleName() + "initialize->enableColoredCMD:false");
//        }
//    }
//    private static boolean enableColoredCMD = false;

    public static void destroy() {
//        if (enableColoredCMD) {
//            reset();
//            AnsiConsole.systemUninstall();
//        }
    }

//    private static void reset() {
//        if (enableColoredCMD) {
////            System.out.print(Ansi.ansi().fg(Color.WHITE).boldOff());
//            System.out.print(Ansi.ansi().reset());
//        }
//    }

    public static void result(CharSequence text) {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().fg(Color.GREEN).bold().a(text));
//            reset();
//        } else {
            System.out.println(text);
//        }
    }

    public static void error(CharSequence text) {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().fg(Color.RED).bold().a(text));
//            reset();
//        } else {
            System.out.println(text);
//        }
    }

    public static void info(CharSequence text) {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().fg(Color.YELLOW).bold().a(text));
//            reset();
//        } else {
            System.out.println(text);
//        }
    }

    public static void link(CharSequence text) {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().fg(Color.BLUE).bold().a(text));
//            reset();
//        } else {
            System.out.println(text);
//        }
    }

    public static void plain(CharSequence text) {
//        if (enableColoredCMD) {
//            reset();
//            System.out.println(text);
//        } else {
            System.out.println(text);
//        }
    }

    public static void hidden(CharSequence text) {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().fg(Color.BLACK).bold().a(text));
//            reset();
//        } else {
            System.out.println(text);
//        }
    }

    public static void clear() {
//        if (enableColoredCMD) {
//            System.out.println(Ansi.ansi().eraseScreen());
//        }
    }
}
