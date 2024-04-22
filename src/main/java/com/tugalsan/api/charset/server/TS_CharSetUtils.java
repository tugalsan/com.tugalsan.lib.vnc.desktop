package com.tugalsan.api.charset.server;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

public class TS_CharSetUtils {

    public static String localeLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static boolean localeTurkish() {
        return localeLanguage().equals("tr");
    }

    public static void setDefaultLocaleToTurkish() {
        Locale.forLanguageTag("tr-TR");
    }

    public static boolean isASCIIPrintable(CharSequence text) {
        return text.codePoints().allMatch(c -> c > 31 && c < 127);
    }

    public static boolean isASCII(CharSequence text) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(text);
    }

    public static String makePrintable(CharSequence text) {
        if (text == null) {
            return "";
        }
        return text.toString().replaceAll("\\P{Print}", "");
    }

    public static boolean isPrintable_slow(CharSequence text) {
        return Objects.equals(text, makePrintable(text));
    }
}
