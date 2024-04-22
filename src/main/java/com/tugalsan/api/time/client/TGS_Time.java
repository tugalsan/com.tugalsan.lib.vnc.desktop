package com.tugalsan.api.time.client;

import com.tugalsan.api.cast.client.*;
import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.*;

public class TGS_Time implements Serializable/*implements IsSerializable*/ {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TGS_Time)) {
            return false;
        }
        var other = (TGS_Time) obj;
        return Objects.equals(second, other.second)
                && Objects.equals(minute, other.minute)
                && Objects.equals(hour, other.hour)
                && Objects.equals(day, other.day)
                && Objects.equals(month, other.month)
                && Objects.equals(year, other.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(second, minute, hour, day, month, year);
    }

//import com.google.gwt.core.client.JsDate;
//NO NEED FOR JsDate :)
//    public TGS_Time(JsDate date) {
//        setDateAndTimeByDate(date);//JsDate.create()
//    }
//    public final void setDateAndTimeByDate(JsDate date) {
//        second = date.getSeconds();//Calendar.getInstance().get(Calendar.SECOND);
//        minute = date.getMinutes();//Calendar.getInstance().get(Calendar.MINUTE);
//        hour = date.getHours();//Calendar.getInstance().get(Calendar.HOUR);
//        day = date.getDate();//Calendar.getInstance().get(Calendar.DATE);
//        month = date.getMonth() + 1;//Calendar.getInstance().get(Calendar.MONTH) + 1;
//        year = date.getFullYear();//Calendar.getInstance().get(Calendar.YEAR);
//        incrementHour(FIX_HOUR);
//    }
    private TGS_Time() {
        setDateAndTimeByDate(new Date());
    }

    private TGS_Time(Date date) {
        setDateAndTimeByDate(date);
    }

    public static long FIX_TimeUTCOffset = 0L;
    public static int FIX_TimeZoneOffset = 0;

    public static TGS_Time of() {
        return new TGS_Time();
    }

    public static TGS_Time ofDayAgo(int positiveDays) {
        var now = of();
        now.incrementDay(-positiveDays);
        return now;
    }

    public static TGS_Time ofDayNext(int positiveDays) {
        var now = of();
        now.incrementDay(positiveDays);
        return now;
    }

    public static TGS_Time ofYearAgo(int positiveYears) {
        var now = of();
        now.incrementYear(-positiveYears);
        return now;
    }

    public static TGS_Time ofYearNext(int positiveYears) {
        var now = of();
        now.incrementYear(positiveYears);
        return now;
    }

    public static TGS_Time ofMinutesAgo(int positiveMinutes) {
        var now = of();
        now.incrementMinute(-positiveMinutes);
        return now;
    }

    public static TGS_Time ofMinutesNext(int positiveMinutes) {
        var now = of();
        now.incrementMinute(positiveMinutes);
        return now;
    }

    public static TGS_Time ofSecondsAgo(int positiveSeconds) {
        var now = of();
        now.incrementSecond(-positiveSeconds);
        return now;
    }

    public static TGS_Time ofSecondsNext(int positiveSeconds) {
        var now = of();
        now.incrementSecond(positiveSeconds);
        return now;
    }

    public static TGS_Time ofMillis(long millis) {
        return of(new Date(millis));
    }

    public static TGS_Time ofDate(Long date) {
        if (date == null) {
            return null;
        }
        var o = new TGS_Time();
        o.setDate(date);
        return o;
    }

    public static TGS_Time ofDateLong(CharSequence date) {
        return ofDate(TGS_CastUtils.toLong(date));
    }

    public static TGS_Time ofTime(Long time) {
        if (time == null) {
            return null;
        }
        var o = new TGS_Time();
        o.setTime(time);
        return o;
    }

    public static TGS_Time ofTimeLong(CharSequence date) {
        return ofTime(TGS_CastUtils.toLong(date));
    }

    public static TGS_Time ofDateAndTime(Long date, Long time) {
        if (date == null || time == null) {
            return null;
        }
        var o = new TGS_Time();
        o.setDate(date);
        o.setTime(time);
        return o;
    }

    public static TGS_Time of(Date date) {
        if (date == null) {
            return null;
        }
        return new TGS_Time(date);
    }

    public static boolean isDate(CharSequence date) {
        if (date == null) {
            return false;
        }
        var dateStr = date.toString();
        return "00.00.0000".length() == dateStr.length()
                && dateStr.indexOf('.', 0) == 2
                && dateStr.indexOf('.', 3) == 5;
    }

    public static String reverseDate(CharSequence date) {
        if (date == null) {
            return null;
        }
        var dateStr = date.toString();
        return dateStr.substring(6) + "." + dateStr.substring(3, 5) + "." + dateStr.substring(0, 2);
    }

    //09.01.2021 20:54:23
    public static TGS_Time ofCalender_DD_MM_YYYY_HH_MM_SS(CharSequence calenderText, char dateDelim) {
        if (calenderText == null) {
            return null;
        }
        var split = calenderText.toString().split(" ");
        if (split.length != 2) {
            return null;
        }
        var date = ofDate(split[0], dateDelim);
        var time = ofTime_HH_MM_SS(split[1]);
        if (date == null || time == null) {
            return null;
        }
        return time.setDate(date.getDate());
    }

    public final boolean isProperTime(boolean zeroIsProper) {
        if (!zeroIsProper && getTime() == 0) {
            return false;
        }
        return getTime() <= 235959 && getTime() >= 0;
    }

    public final boolean isProperDate() {
        var maxMonthDays = 0;
        switch (month) {//DONT USE SWITCH EXPRESSIONS! GWT WONT LIKE U
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                maxMonthDays = 31;
                break;
            case 2:
                maxMonthDays = year % 4 == 0 ? 29 : 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                maxMonthDays = 30;
                break;
            default:
        }
        return day >= 0 && day <= maxMonthDays && month >= 0 && month <= 12;
    }

    public final int getWeekNumber() {
        var tmp = cloneIt();
        tmp.setDay(1);
        tmp.setMonth(1);
        //tmp.setYear(2018);
        var dayOfWeekInit = dayOfWeek(tmp);
        var dayOfYearFix = getDayOfYear() + (dayOfWeekInit - 1);
        var weekNumber = ((dayOfYearFix - 1) / 7) + 1;
        if (dayOfWeekInit != 1) {
            weekNumber--;
        }
        return weekNumber;
    }

    public final static int dayOfWeek(int day, int month, int year) {// Returns 1-7
        if (month < 3) {
            month += 12;
            year--;
        }
        int dow = (day + (int) ((month + 1) * 2.6) + year + (int) (year / 4) + 6 * (int) (year / 100) + (int) (year / 400) + 6) % 7;
        return dow == 0 ? 7 : dow;
    }

    public final int dayOfWeek() {//1-7
        return dayOfWeek(this);
    }

    public final static int dayOfWeek(TGS_Time date) {//1-7
        return dayOfWeek(date.getDay(), date.getMonth(), date.getYear());
    }

    public final static TGS_Time dayOfWeek_getMonday(TGS_Time date) {//1-7
        return dayOfWeek_getMonday(date.getDay(), date.getMonth(), date.getYear());
    }

    public final static TGS_Time dayOfWeek_getMonday(int day, int month, int year) {//1-7
        var monday = new TGS_Time();
        monday.setDay(day);
        monday.setMonth(month);
        monday.setYear(year);
        var dayofWeek = dayOfWeek(monday);
        while (dayofWeek != 1) {
            monday.incrementDay(-1);
            dayofWeek = dayOfWeek(monday);
        }
        return monday;
    }

    public final static TGS_Time dayOfWeek_getSunday(TGS_Time date) {//1-7
        return dayOfWeek_getSunday(date.getDay(), date.getMonth(), date.getYear());
    }

    public final static TGS_Time dayOfWeek_getSunday(int day, int month, int year) {//1-7
        var sunday = new TGS_Time();
        sunday.setDay(day);
        sunday.setMonth(month);
        sunday.setYear(year);
        var dayofWeek = dayOfWeek(sunday);
        while (dayofWeek != 7) {
            sunday.incrementDay(1);
            dayofWeek = dayOfWeek(sunday);
        }
        return sunday;
    }

    @SuppressWarnings("deprecation")
    public final TGS_Time setDateAndTimeByDate(Date date) {//DO NOT TOUCH IT: WORKS ON BOTH SERVER AND CLIENT SIDE!
        if (date == null) {
            return null;
        }
        if (FIX_TimeUTCOffset != 0L) {
            date = new Date(date.getTime() + FIX_TimeUTCOffset);
        }
        second = date.getSeconds();
        minute = date.getMinutes();
        hour = date.getHours();
        day = date.getDate();
        month = date.getMonth() + 1;
        year = date.getYear() + 1900;
        if (FIX_TimeZoneOffset != 0) {
            incrementHour(FIX_TimeZoneOffset);
        }
        return this;
    }

    public final Date toDateObject() {
        var date = new Date();
        date.setSeconds(second);
        date.setMinutes(minute);
        date.setHours(hour);
        date.setDate(day);
        date.setMonth(month - 1);
        date.setYear(year - 1900);
        return date;
    }

    public final long toDateMillis() {
        return toDateObject().getTime();
    }

    public final TGS_Time setDateByDate(Date date) {
        var backupSeconds = second;
        var backupMintues = minute;
        var backupHours = hour;
        setDateAndTimeByDate(date);
        second = backupSeconds;
        minute = backupMintues;
        hour = backupHours;
        return this;
    }

    public final TGS_Time setTimeByDate(Date date) {
        var backupDays = day;
        var backupMonths = month;
        var backupYears = year;
        setDateAndTimeByDate(date);
        day = backupDays;
        month = backupMonths;
        year = backupYears;
        return this;
    }

    public static int getCurrentHour() {
        return new TGS_Time().getHour();
    }

    public static int getCurrentMinute() {
        return new TGS_Time().getMinute();
    }

    public static int getCurrentSecond() {
        return new TGS_Time().getSecond();
    }

    public static int getCurrentDay() {
        return new TGS_Time().getDay();
    }

    public static int getCurrentMonth() {
        return new TGS_Time().getMonth();
    }

    public static int getCurrentYear() {
        return new TGS_Time().getYear();
    }

    public static long getCurrentDate() {
        return new TGS_Time().getDate();
    }

    public static long getCurrentTime() {
        return new TGS_Time().getTime();
    }

    public static TGS_Time getBeginningOfThisYear() {
        return ofDate(TGS_Time.getCurrentYear() * 10000 + 1L * 100 + 1);
    }

    public static TGS_Time getEndingOfThisYear() {
        return ofDate(TGS_Time.getCurrentYear() * 10000 + 12L * 100 + 31);
    }

    public static TGS_Time ofTime_HH_MM_SS(CharSequence time) {
        return TGS_UnSafe.call(() -> {
            if (time == null || time.length() != 8) {
                return null;
            }
            var hri = TGS_CastUtils.toInteger(time.toString().substring(0, 2));
            if (hri == null) {
                return null;
            }
            var mni = TGS_CastUtils.toInteger(time.toString().substring(3, 5));
            if (mni == null) {
                return null;
            }
            var sci = TGS_CastUtils.toInteger(time.toString().substring(6, 8));
            if (sci == null) {
                return null;
            }
            var t = new TGS_Time();
            t.setHour(hri);
            t.setMinute(mni);
            t.setSecond(sci);
            return t;
        }, e -> null);
    }

    public static TGS_Time ofTime_MM_SS(CharSequence time) {
        return TGS_UnSafe.call(() -> {
            var time0 = time;
            if (time0 == null) {
                return null;
            }
            time0 = time0.toString().trim();
            var i = time0.toString().indexOf(" ");
            if (i == -1) {
                i = time0.toString().indexOf(":");
                if (i == -1) {
                    var ii = TGS_CastUtils.toInteger(time0);
                    if (ii == null) {
                        return null;
                    } else {
                        TGS_Time rt = new TGS_Time();
                        rt.setHour(0);
                        rt.setMinute(ii);
                        rt.setSecond(0);
                        return rt;
                    }
                }
            }
            var mni = TGS_CastUtils.toInteger(time0.toString().substring(0, i));
            if (mni == null) {
                return null;
            }
            var sci = TGS_CastUtils.toInteger(time0.toString().substring(i + 1));
            if (sci == null) {
                return null;
            }
            var t = new TGS_Time();
            t.setHour(0);
            t.setMinute(mni);
            t.setSecond(sci);
            return t;
        }, e -> null);
    }

    public static TGS_Time ofTime_HH_MM(CharSequence time) {
        return TGS_UnSafe.call(() -> {
            var time0 = time;
            if (time0 == null) {
                return null;
            }
            time0 = time0.toString().trim();
            var i = time0.toString().indexOf(" ");
            if (i == -1) {
                i = time0.toString().indexOf(":");
                if (i == -1) {
                    var ii = TGS_CastUtils.toInteger(time0);
                    if (ii == null) {
                        return null;
                    } else {
                        TGS_Time rt = new TGS_Time();
                        rt.setHour(ii);
                        rt.setMinute(0);
                        rt.setSecond(0);
                        return rt;
                    }
                }
            }
            var hri = TGS_CastUtils.toInteger(time0.toString().substring(0, i));
            if (hri == null) {
                return null;
            }
            var mni = TGS_CastUtils.toInteger(time0.toString().substring(i + 1));
            if (mni == null) {
                return null;
            }
            var t = new TGS_Time();
            t.setHour(hri);
            t.setMinute(mni);
            t.setSecond(0);
            return t;
        }, e -> null);
    }

    public String toString_YYYYMMDD_HHMMSS() {
        return TGS_StringIntegerUtils.make4Chars(year)
                + TGS_StringIntegerUtils.make2Chars(month)
                + TGS_StringIntegerUtils.make2Chars(day)
                + " "
                + TGS_StringIntegerUtils.make2Chars(hour)
                + TGS_StringIntegerUtils.make2Chars(minute)
                + TGS_StringIntegerUtils.make2Chars(second);
    }

    public String toString_YYYY_MM_DD() {
        return TGS_StringIntegerUtils.make4Chars(year) + "-" + TGS_StringIntegerUtils.make2Chars(month) + "-" + TGS_StringIntegerUtils.make2Chars(day);
    }

    public String toString_YYYY_MM() {
        return TGS_StringIntegerUtils.make4Chars(year) + "-" + TGS_StringIntegerUtils.make2Chars(month);
    }

    public static TGS_Time ofDate_YYYY_MM_DD(String YYYY_MM_DD) {
        if (YYYY_MM_DD == null || "YYYY_MM_DD".length() != YYYY_MM_DD.length()) {
            return null;
        }
        var year = TGS_CastUtils.toInteger(YYYY_MM_DD.substring(0, 4));
        if (year == null) {
            return null;
        }
        var month = TGS_CastUtils.toInteger(YYYY_MM_DD.substring(5, 7));
        if (month == null) {
            return null;
        }
        var day = TGS_CastUtils.toInteger(YYYY_MM_DD.substring(8, 10));
        if (day == null) {
            return null;
        }
        return ofDate(year * 10000 + month * 100L + day);
    }

    public static TGS_Time ofDate(String date) {
        return TGS_UnSafe.call(() -> {
            TGS_Time d;
            d = ofDate(date, ' ');
            if (d == null) {
                d = ofDate(date, '/');
            }
            if (d == null) {
                d = ofDate(date, '.');
            }
            if (d == null) {
                d = ofDate(date, '-');
            }
            return d;
        }, e -> null);
    }

    public static TGS_Time ofDate(String date, char delim) {
        return TGS_UnSafe.call(() -> {
            var date0 = date;
            if (date0 == null) {
                return null;
            }
            date0 = date0.trim();
            var i = date0.indexOf(delim);
            if (i == -1) {
                return null;
//            return new TK_GWTDate();
            }
            var j = date0.lastIndexOf(delim);
            Integer dyi;
            if (date0.length() >= i) {
                dyi = TGS_CastUtils.toInteger(date0.substring(0, i));
            } else {
                dyi = getCurrentDay();
            }
            if (dyi == null) {
                return null;
            }
            Integer mni;
            if (date0.length() >= j) {
                mni = TGS_CastUtils.toInteger(date0.substring(i + 1, j));
            } else {
                mni = getCurrentMonth();
            }
            if (mni == null) {
                return null;
            }
            Integer yri;
            if (date0.length() > j + 1) {
                yri = TGS_CastUtils.toInteger(date0.substring(j + 1, date0.length()));
            } else {
                yri = getCurrentMonth();
            }
            if (yri == null) {
                return null;
            }
            var d = new TGS_Time();
            d.setDay(dyi);
            d.setMonth(mni);
            d.setYear(yri < 100 ? yri + 2000 : yri);
            return d;
        }, e -> null);
    }

    public String getDateStamp() {
        return TGS_StringUtils.concat(String.valueOf(year), "-", TGS_StringIntegerUtils.make2Chars(month), "-", TGS_StringIntegerUtils.make2Chars(day), " 00:00:00.000");
    }

    public static int getMaxDaysOfYear(int year) {
        return IntStream.rangeClosed(1, 12).map(i -> getMonthLength(i, year)).sum();
    }

    public static String getDayOfWeekName(TGS_CharSetCast.Locale2Cast locale2Cast, int dayOfWeek) {
        return locale2Cast == TGS_CharSetCast.Locale2Cast.TURKISH
                ? new String[]{"Pazartesi", "Sali", "Carsamba", "Persembe", "Cuma", "Cumartesi", "Pazar"}[dayOfWeek - 1]
                : new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}[dayOfWeek - 1];
    }

    public static int getMonthLength(int month, int year) {
        if (month < 1 || month > 12) {
            return 0;
        }
        var monthLength = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (month != 2) {
            return monthLength[month - 1];
        }
        var i = year % 4;
        if (i == 0) {
            return monthLength[month - 1] + 1;
        } else {
            return monthLength[month - 1];
        }
    }

    //@Override
    public TGS_Time cloneIt() {
        return TGS_Time.ofDateAndTime(getDate(), getTime());
    }
    private int day, month, year, hour, minute, second;

    public TGS_Time setDay(int day) {
        this.day = day;
        return this;
    }

    public int getDay() {
        return day;
    }

    public TGS_Time setMonth(int month) {
        this.month = month;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public TGS_Time setYear(int year) {
        this.year = year;
        return this;
    }

    public int getYear() {
        return year;
    }

    public int getHour() {
        return hour;
    }

    public TGS_Time setHour(int hour) {
        this.hour = hour;
        return this;
    }

    public int getMinute() {
        return minute;
    }

    public TGS_Time setMinute(int minute) {
        this.minute = minute;
        return this;
    }

    public int getSecond() {
        return second;
    }

    public TGS_Time setSecond(int second) {
        this.second = second;
        return this;
    }

    public int getDayOfYear() {
        var d = ofDate(20100101L);
        d.setYear(year);
        var diff = 1;
        while (d.hasSmallerDateThan(this)) {
            d.incrementDay(1);
            diff++;
        }
        return diff;
    }

    public String toString_dateOnly() {
        return toString_dateOnly(getDate());
    }

    public String toString_timeOnly() {
        return toString_timeOnly(getTime());
    }

    public String toString_timeOnly_simplified() {
        return toString_timeOnly_simplified(getTime());
    }

    @Override
    public String toString() {
        return TGS_StringUtils.concat(toString_dateOnly(), " ", toString_timeOnly());
    }

    public static String toString_now() {
        return TGS_StringUtils.concat(toString_dateOnly_today(), " ", toString_timeOnly_now());
    }

    public static String toString(long date, long time) {
        return TGS_StringUtils.concat(toString_dateOnly(date), " ", toString_timeOnly(time));
    }

    public static String toString_dateOnly_today() {
        return toString_dateOnly(getCurrentDate());
    }

    public static String toString_dateOnly(long date) {
        var d = parseLastTwoDecimal(date);
        date -= (long) d;
        date /= 100L;
        var m = parseLastTwoDecimal(date);
        date -= (long) m;
        date /= 100L;
        var y = (int) date;
        return TGS_StringUtils.concat(TGS_StringIntegerUtils.make2Chars(d), ".", TGS_StringIntegerUtils.make2Chars(m), "." + TGS_StringIntegerUtils.make4Chars(y));
    }

    public static String toString_timeOnly_now() {
        return toString_timeOnly(getCurrentTime());
    }

    public static String toString_timeOnly_now_simplified() {
        return toString_timeOnly(getCurrentTime()).substring(0, 5);
    }

    public static String toString_timeOnly_simplified(long time) {
        return toString_timeOnly(time).substring(0, 5);
    }

    public static String toString_timeOnly(long time) {
        var s = parseLastTwoDecimal(time);
        time -= (long) s;
        time /= 100L;
        var m = parseLastTwoDecimal(time);
        time -= (long) m;
        time /= 100L;
        var h = (int) time;
        return TGS_StringUtils.concat(TGS_StringIntegerUtils.make2Chars(h), ":", TGS_StringIntegerUtils.make2Chars(m), ":", TGS_StringIntegerUtils.make2Chars(s));
    }

    @Deprecated //JUST CREATE A NEW OBJECT
    public final TGS_Time setTimeNow() {
        var now = new TGS_Time();
        second = now.second;
        minute = now.minute;
        hour = now.hour;
        return this;
    }

    public final TGS_Time setTimeNull() {
        second = 00;
        minute = 99;
        hour = 99;
        return this;
    }

    public final boolean isTimeNull() {
        return second == 0 && minute == 99 && hour == 99;
    }

    @Deprecated //JUST CREATE A NEW OBJECT
    public final TGS_Time setDateToday() {
        var now = new TGS_Time();
        day = now.day;
        month = now.month;
        year = now.year;
        return this;
    }

    public final TGS_Time setDateEmpty() {
        day = 0;
        month = 0;
        year = 2000;
        return this;
    }

    public final boolean isDateEmpty() {
        return day == 0 && month == 0 && year == 2000;
    }

    public final TGS_Time setToTodayAndNow() {
        setDateAndTimeByDate(new Date());
        return this;
    }

    public long getSecondsDifference(TGS_Time toTime) {
        var secs = 0L;
        if (toTime.hasGreaterTimeThan(this)) {
            while (toTime.hasGreaterTimeThan(this)) {
                secs++;
                incrementSecond(1);
            }
            if (secs != 0L) {
                LongStream.range(0, secs).forEachOrdered(l -> incrementSecond(-1));
            }
        } else {
            while (toTime.hasSmallerTimeThan(this)) {
                secs--;
                incrementSecond(-1);
            }
            if (secs != 0L) {
                LongStream.range(secs, 0).forEachOrdered(l -> incrementSecond(1));
            }
        }
        return secs;
    }

    public TGS_Time incrementSecond(int secondStep) {
        second += secondStep;
        while (second < 0) {
            second += 60;
            incrementMinute(-1);
        }
        while (second > 60) {
            second -= 60;
            incrementMinute(+1);
        }
        return this;
    }

    public TGS_Time incrementMinute(int minuteStep) {
        minute += minuteStep;
        while (minute < 0) {
            minute += 60;
            incrementHour(-1);
        }
        while (minute > 60) {
            minute -= 60;
            incrementHour(+1);
        }
        return this;
    }

    public TGS_Time incrementHour(int hourStep) {
        hour += hourStep;
        while (hour < 0) {
            hour += 24;
            incrementDay(-1);
        }
        while (hour > 24) {
            hour -= 24;
            incrementDay(+1);
        }
        return this;
    }

    public TGS_Time incrementDay(int dayStep) {
        var directionForward = dayStep > 0;
        dayStep = Math.abs(dayStep);
        if (directionForward) {
            while (true) {
                var dayToNextMonth = getMonthLength(month, year) - day + 1;
                if (dayStep >= dayToNextMonth) {
                    dayStep -= dayToNextMonth;
                    day = 1;
                    month++;
                    if (month == 13) {
                        month = 1;
                        year++;
                    }
                } else {
                    day += dayStep;
                    return this;
                }
            }
        }
        while (true) {
            if (dayStep >= day) {
                dayStep -= day;
                month--;
                if (month == 0) {
                    month = 12;
                    year--;
                }
                day = getMonthLength(month, year);
            } else {
                day -= dayStep;
                return this;
            }
        }
    }

    private static int parseLastTwoDecimal(long value) {
        while (value >= 100000000L) {
            value -= 100000000L;
        }
        while (value >= 10000000L) {
            value -= 10000000L;
        }
        while (value >= 1000000L) {
            value -= 1000000L;
        }
        while (value >= 100000L) {
            value -= 100000L;
        }
        while (value >= 10000L) {
            value -= 10000L;
        }
        while (value >= 1000L) {
            value -= 1000L;
        }
        while (value >= 100L) {
            value -= 100L;
        }
        return (int) value;
    }

    public final TGS_Time setDate(long date) {
        day = parseLastTwoDecimal(date);
        date -= (long) day;
        date /= 100L;
        month = parseLastTwoDecimal(date);
        date -= (long) month;
        date /= 100L;
        year = (int) date;
        return this;
    }

    public final TGS_Time setTime(long time) {
        second = parseLastTwoDecimal(time);
        time -= (long) second;
        time /= 100L;
        minute = parseLastTwoDecimal(time);
        time -= (long) minute;
        time /= 100L;
        hour = (int) time;
        return this;
    }

    public long getDate() {
        return (long) day + (long) month * 100L + (long) year * 10000L;
    }

    public long getTime() {
        return (long) second + (long) minute * 100L + (long) hour * 10000L;
    }

    public boolean hasGreaterDateThan(TGS_Time date) {
        if (date == null) {
            return false;
        }
        return getDate() > date.getDate();
    }

    public boolean hasGreaterDateThanOrEqual(TGS_Time date) {
        if (date == null) {
            return false;
        }
        return getDate() >= date.getDate();
    }

    public boolean hasGreaterTimeThan(TGS_Time time) {
        if (time == null) {
            return false;
        }
        return getTime() > time.getTime();
    }

    public boolean hasGreaterTimeThanOrEqual(TGS_Time time) {
        if (time == null) {
            return false;
        }
        return getTime() >= time.getTime();
    }

    public boolean hasSmallerDateThan(TGS_Time date) {
        if (date == null) {
            return false;
        }
        return getDate() < date.getDate();
    }

    public boolean hasSmallerDateThanOrEqual(TGS_Time date) {
        if (date == null) {
            return false;
        }
        return getDate() <= date.getDate();
    }

    public boolean hasSmallerTimeThan(TGS_Time time) {
        if (time == null) {
            return false;
        }
        return getTime() < time.getTime();
    }

    public boolean hasSmallerTimeThanOrEqual(TGS_Time time) {
        if (time == null) {
            return false;
        }
        return getTime() <= time.getTime();
    }

    public boolean hasEqual(TGS_Time date) {
        return hasEqualDateWith(date) && hasEqualTimeWith(date);
    }

    public boolean hasGreater(TGS_Time date) {
        return hasGreaterDateThan(date) || (hasEqualDateWith(date) && hasGreaterTimeThan(date));
    }

    public boolean hasSmaller(TGS_Time date) {
        return hasSmallerDateThan(date) || (hasEqualDateWith(date) && hasSmallerTimeThan(date));
    }

    public boolean hasGreaterOrEqual(TGS_Time date) {
        return hasGreater(date) || hasEqual(date);
    }

    public boolean hasSmallerOrEqual(TGS_Time date) {
        return hasSmaller(date) || hasEqual(date);
    }

    public boolean hasEqualDateWith(TGS_Time date) {
        if (date == null) {
            return false;
        }
        return date.getDate() == getDate();
    }

    public boolean hasEqualTimeWith(TGS_Time time) {
        if (time == null) {
            return false;
        }
        return time.getTime() == getTime();
    }

    public TGS_Time incrementMonth(int i) {
        while (i != 0) {
            if (i < 0) {
                month--;
                i++;
                if (month < 1) {
                    year--;
                    month = 12;
                }
            } else {
                month++;
                i--;
                if (month > 12) {
                    month = 1;
                    year++;
                }
            }
        }
        return this;
    }

    public TGS_Time incrementYear(int i) {
        year += i;
        return this;
    }

    public int timeDifferenceInSeconds(TGS_Time time2, boolean add24HoursIfMinus) {
        if (this.hasGreaterTimeThan(time2)) {//reverse
            if (add24HoursIfMinus) {
                time2.setHour(time2.getHour() + 24);
            } else {
                return time2.timeDifferenceInSeconds(this, add24HoursIfMinus);
            }
        }
        return (time2.getHour() - this.getHour()) * 60 * 60 + (time2.getMinute() - this.getMinute()) * 60 + (time2.getSecond() - this.getSecond());
    }

    public Long dayDifference(TGS_Time date2) {
        if (date2 == null) {
            return null;
        }
        var dayDiff = 0L;
        TGS_Time clone;
        if (date2.hasGreaterDateThan(this)) {
            clone = this.cloneIt();
            while (clone.hasSmallerDateThan(date2)) {
                clone.incrementDay(1);
                dayDiff++;
            }
            return dayDiff - 1l;
        } else if (date2.hasSmallerDateThan(this)) {
            clone = date2.cloneIt();
            while (clone.hasSmallerDateThan(this)) {
                clone.incrementDay(1);
                dayDiff++;
            }
            return dayDiff - 1l;
        } else {
            return dayDiff;
        }
    }

    public TGS_Time getStartOfYear() {
        var d = cloneIt();
        d.day = 1;
        d.month = 1;
        return d;
    }

    public TGS_Time getEndOfYear() {
        var d = cloneIt();
        d.day = 31;
        d.month = 12;
        return d;
    }

    public TGS_Time getStartOfMonth() {
        var d = cloneIt();
        d.day = 1;
        return d;
    }

    public TGS_Time getEndOfMonth() {
        var d = cloneIt();
        d.day = getMonthLength(month, year);
        return d;
    }

    public TGS_Time getStartOfWeek() {
        var d = cloneIt();
        var day1_7 = TGS_Time.dayOfWeek(d);
        while (day1_7 > 1) {
            d.incrementDay(-1);
            day1_7 = TGS_Time.dayOfWeek(d);
        }
        return d;
    }

    public TGS_Time getEndOfWeek() {
        var d = cloneIt();
        var day1_7 = TGS_Time.dayOfWeek(d);
        while (day1_7 < 7) {
            d.incrementDay(1);
            day1_7 = TGS_Time.dayOfWeek(d);
        }
        return d;
    }

    public boolean isToday() {
        return getDate() == getCurrentDate();
    }

}
