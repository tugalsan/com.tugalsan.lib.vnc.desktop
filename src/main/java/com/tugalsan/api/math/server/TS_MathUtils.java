package com.tugalsan.api.math.server;

public class TS_MathUtils {

    public static Integer percentInt(double numerator, double denominator) {
        var val = percentDbl(numerator, denominator, 0);
        return val == null ? null : val.intValue();
    }

    public static Double percentDbl(double numerator, double denominator, int precision) {
        var val = percentStr(numerator, denominator, precision);
        return val == null ? null : Double.parseDouble(val);
    }

    public static String percentStr(double numerator, double denominator, int precision) {
        if (denominator == 0d) {
            return null;
        }
        if (numerator == 0d) {
            return "0";
        }
        var val = 100d * numerator / denominator;
        if (precision < 0) {
            return String.valueOf(val);
        }
        return String.format("%." + precision + "f", val);//GWT DOES NOT LIKE U
    }
}
