package com.tugalsan.api.time.server;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class TS_TimeUtils {

    public static String toString(Duration dur) {
        return String.format("%d:%02d:%02d",
                dur.toHours(),
                dur.toMinutesPart(),
                dur.toSecondsPart());
    }

    public static Instant toInstant(Duration duration) {
        return Instant.now().plusSeconds(duration.getSeconds());
    }

    public static String getTimeZoneId(TimeZone timeZone) {
        var milliDiff = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        return Arrays.stream(TimeZone.getAvailableIDs()).filter(id -> TimeZone.getTimeZone(id).getRawOffset() == milliDiff).findAny().orElse(null);
    }

    public static ZoneOffset getOffset(TimeZone timeZone) { //for using ZoneOffsett class
        var zi = timeZone.toZoneId();
        var zr = zi.getRules();
        return zr.getOffset(LocalDateTime.now());
    }

    public static int getOffsetHours(TimeZone timeZone) { //just hour offset
        var zo = getOffset(timeZone);
        return (int) TimeUnit.SECONDS.toHours(zo.getTotalSeconds());
    }

//    public static interface WinKernel32 extends StdCallLibrary {
//
//        boolean SetLocalTime(SYSTEMTIME st);
//        WinKernel32 instance = (WinKernel32) Native.load("kernel32.dll", WinKernel32.class);
//    }
//
//    public static boolean setDateAndTime(TGS_Time dateAndTime) {
//        if (Platform.isWindows()) {
//            var st = new SYSTEMTIME();
//            st.wYear = (short) dateAndTime.getYear();
//            st.wMonth = (short) dateAndTime.getMonth();
//            st.wDay = (short) dateAndTime.getDay();
//            st.wHour = (short) dateAndTime.getHour();
//            st.wMinute = (short) dateAndTime.getMinute();
//            st.wSecond = (short) dateAndTime.getSecond();
//            return WinKernel32.instance.SetLocalTime(st);
//        } else {
//            var b1 = run(TGS_StringUtils.concat("date +%Y%m%d -s \"" + dateAndTime.getYear(), make2Chars(dateAndTime.getMonth()), make2Chars(dateAndTime.getDay()), "\""));
//            var b2 = run(TGS_StringUtils.concat("date +%T -s \"", make2Chars(dateAndTime.getHour()), ":", make2Chars(dateAndTime.getMinute()), ":", make2Chars(dateAndTime.getSecond()), "\""));
//            return b1 && b2;
//        }
//    }
//
//    //NO DEP FUNCTION
//    private static String make2Chars(int i) {
//        var is = String.valueOf(i);
//        return is.length() < 2 ? TGS_StringUtils.concat("0", is) : is;
//    }
//
//    //NO DEP FUNCTION
//    private static boolean run(CharSequence commandLine) {
//        return TGS_UnSafe.call(() -> {
//            var p = Runtime.getRuntime().exec(commandLine.toString());
//            p.waitFor();
//            return true;
//        }, e -> {
//            System.out.println(TS_TimeUtils.class.getSimpleName() + "->run(CharSequence \"" + commandLine + "\")");
//            e.printStackTrace();
//            return false;
//        });
//    }
}
