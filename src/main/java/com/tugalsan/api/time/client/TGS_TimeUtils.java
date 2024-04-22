package com.tugalsan.api.time.client;

import com.tugalsan.api.unsafe.client.*;

public class TGS_TimeUtils {

    public static int SECS_TIMEOUT_MINUS_ONE() {
        return -1;
    }

    public static int SECS_TIMEOUT_ZERO() {
        return 0;
    }

    public static int SECS_TIMEOUT_MINUTE() {
        return 60;
    } // 60 seconds * 1 minutes

    public static int SECS_TIMEOUT_HOUR() {
        return SECS_TIMEOUT_MINUTE() * 60;
    } // 60 seconds * 1 minutes

    public static int SECS_TIMEOUT_HOURS_WORK() {
        return SECS_TIMEOUT_HOUR() * 9;
    } // 60 seconds * 60 minutes * 9 hours

    public static int SECS_TIMEOUT_DAY() {
        return SECS_TIMEOUT_HOUR() * 24;
    }// 60 seconds * 60 minutes * 24 hours

    public static long zeroDate() {
        return 20000000L;
    }

    public static boolean isValidOrZeroDate(Long lngDate) {
        return isZeroDate(lngDate) || isValidDate(lngDate);
    }

    public static boolean isZeroDate(Long lngDate) {
        if (lngDate == null) {
            return false;
        }
        return lngDate == zeroDate();
    }

    public static boolean isValidDate(Long lngDate) {
        return TGS_UnSafe.call(() -> {
            if (lngDate == null) {
                return false;
            }
            var date = TGS_Time.ofDate(lngDate);
            var maxMonthDays = 0;
            switch (date.getMonth()) {
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
                    maxMonthDays = date.getMonth() % 4 == 0 ? 29 : 28;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    maxMonthDays = 30;
                    break;
                default:
            }
            return date.getDay() > 0 && date.getDay() <= maxMonthDays && date.getMonth() > 0 && date.getMonth() <= 12;
        }, e -> false);
    }

    public static boolean isValidTime(Long lngTime) {
        return TGS_UnSafe.call(() -> {
            if (lngTime == null) {
                return false;
            }
            var time = TGS_Time.ofTime(lngTime);
            return time.getHour() >= 0 && time.getHour() <= 23 && time.getMinute() >= 0 && time.getMinute() <= 59 && time.getSecond() >= 0 && time.getSecond() <= 59;
        }, e -> false);
    }
}
