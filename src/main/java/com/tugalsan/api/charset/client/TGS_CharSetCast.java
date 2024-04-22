package com.tugalsan.api.charset.client;

import java.nio.charset.Charset;
import java.util.Locale;

public class TGS_CharSetCast {

    /*
        GWT    SIDE INIT: TGS_CharSetCast.LOCALE2CAST = TGC_CharSetUtils.localeTurkish() ? TGS_CharSetCast.Locale2Cast.TURKISH : TGS_CharSetCast.Locale2Cast.OTHER;
        SERVER SIDE INIT: TGS_CharSetCast.LOCALE2CAST = TS_CharSetUtils.localeTurkish()  ? TGS_CharSetCast.Locale2Cast.TURKISH : TGS_CharSetCast.Locale2Cast.OTHER;
     */
    public static enum Locale2Cast {
        TURKISH, OTHER
    }

    public static Locale2Cast LOCALE2CAST = Locale2Cast.TURKISH;

    //ISSUE: https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/lang/String.html#toLowerCase()
    public static String toLocaleLowerCase(CharSequence source) {
        if (source == null) {
            return null;
        }
        return LOCALE2CAST == Locale2Cast.TURKISH
                ? source.toString().toLowerCase(Locale.ROOT)
                : source.toString().toLowerCase();
    }

    //ISSUE: https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/lang/String.html#toUpperCase()
    public static String toLocaleUpperCase(CharSequence source) {
        if (source == null) {
            return null;
        }
        return LOCALE2CAST == Locale2Cast.TURKISH
                ? source.toString().toUpperCase(Locale.ROOT)
                : source.toString().toUpperCase();
    }

    public static boolean equalsLocaleIgnoreCase(CharSequence item0, CharSequence item1) {
        if (item0 == null && item1 == null) {
            return true;
        }
        if (item0 == null && item1 != null) {
            return false;
        }
        if (item0 != null && item1 == null) {
            return false;
        }
        return toLocaleUpperCase(item0).equals(toLocaleUpperCase(item1));
    }

    public static boolean containsLocaleIgnoreCase(CharSequence fullContent, CharSequence searchTag) {
        if (fullContent == null && searchTag == null) {
            return true;
        }
        if (fullContent == null && searchTag != null) {
            return false;
        }
        if (fullContent != null && searchTag == null) {
            return false;
        }
        return toLocaleUpperCase(fullContent).contains(toLocaleUpperCase(searchTag));
    }

    public static boolean endsWithLocaleIgnoreCase(CharSequence fullContent, CharSequence endsWithTag) {
        if (fullContent == null && endsWithTag == null) {
            return true;
        }
        if (fullContent == null && endsWithTag != null) {
            return false;
        }
        if (fullContent != null && endsWithTag == null) {
            return false;
        }
        return toLocaleUpperCase(fullContent).endsWith(toLocaleUpperCase(endsWithTag));
    }

    public static boolean startsWithLocaleIgnoreCase(CharSequence fullContent, CharSequence startsWithTag) {
        if (fullContent == null && startsWithTag == null) {
            return true;
        }
        if (fullContent == null && startsWithTag != null) {
            return false;
        }
        if (fullContent != null && startsWithTag == null) {
            return false;
        }
        return toLocaleUpperCase(fullContent).startsWith(toLocaleUpperCase(startsWithTag));
    }

    public static String to(CharSequence source, Charset sourceCharset, Charset destCharset) {
        return new String(source.toString().getBytes(sourceCharset), destCharset);
    }
}
