package com.tugalsan.api.log.server;

import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
//import org.fusesource.jansi.Ansi.Color;
//import org.fusesource.jansi.*;

public class TS_LogUtils {

    public static PrintWriter printWriterForWindows() {
        return TGS_UnSafe.call(() -> {
            if (pritWriter == null) {
                pritWriter = new PrintWriter(new OutputStreamWriter(System.out, "Cp850"));
            }
            return pritWriter;
        });
    }
    @Deprecated //NOT TESTED
    public static boolean ENABLE_Cp850_FOR_WINDOWS = false;
    private static volatile PrintWriter pritWriter = null;

    private static void print(Appendable text) {
        if (ENABLE_Cp850_FOR_WINDOWS) {
            pritWriter.print(text);
        } else {
            System.out.print(text);
        }
    }

    private static void println(Appendable text) {
        if (ENABLE_Cp850_FOR_WINDOWS) {
            pritWriter.println(text);
        } else {
            System.out.println(text);
        }
    }
    
    private static void print(CharSequence text) {
        if (ENABLE_Cp850_FOR_WINDOWS) {
            pritWriter.print(text);
        } else {
            System.out.print(text);
        }
    }

    private static void println(CharSequence text) {
        if (ENABLE_Cp850_FOR_WINDOWS) {
            pritWriter.println(text);
        } else {
            System.out.println(text);
        }
    }

    public static void setColoredConsole(boolean enableColoredCMD) {
        TS_LogUtils.enableColoredCMD = enableColoredCMD;
//        if (enableColoredCMD) {
//            AnsiConsole.systemInstall();
//            reset();
//            result(TS_LogUtils.class.getSimpleName() + ".initialize->enableColoredCMD:true");
//        } else {
            result(TS_LogUtils.class.getSimpleName() + "initialize->enableColoredCMD:false");
//        }
    }
    private static boolean enableColoredCMD = false;

//    public static void destroy() {
//        if (enableColoredCMD) {
//            reset();
//            AnsiConsole.systemUninstall();
//        }
//    }

//    private static void reset() {
//        if (enableColoredCMD) {
////            print(Ansi.ansi().fg(Color.WHITE).boldOff());
//            print(Ansi.ansi().reset());
//        }
//    }

    public static void result(CharSequence text) {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().fg(Color.GREEN).bold().a(text));
//            reset();
//        } else {
            println(text);
//        }
    }

    public static void error(CharSequence text) {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().fg(Color.RED).bold().a(text));
//            reset();
//        } else {
            println(text);
//        }
    }

    public static void info(CharSequence text) {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().fg(Color.YELLOW).bold().a(text));
//            reset();
//        } else {
            println(text);
//        }
    }

    public static void link(CharSequence text) {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().fg(Color.BLUE).bold().a(text));
//            reset();
//        } else {
            println(text);
//        }
    }

    public static void plain(CharSequence text) {
//        if (enableColoredCMD) {
//            reset();
//            println(text);
//        } else {
            println(text);
//        }
    }

    public static void hidden(CharSequence text) {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().fg(Color.BLACK).bold().a(text));
//            reset();
//        } else {
            println(text);
//        }
    }

//    public static void clear() {
//        if (enableColoredCMD) {
//            println(Ansi.ansi().eraseScreen());
//        }
//    }
}
