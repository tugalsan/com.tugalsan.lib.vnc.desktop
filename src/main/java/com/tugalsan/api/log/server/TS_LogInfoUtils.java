package com.tugalsan.api.log.server;

public class TS_LogInfoUtils {

    //var st = Thread.currentThread().getStackTrace();
    public static String of(StackTraceElement[] st) {
        if (st.length == 0) {
            return "null";
        }
        var i = st.length - 1;
        return st[i].getClassName() + "->" + st[i].getMethodName() + "->" + st[i].getLineNumber();
    }
}
