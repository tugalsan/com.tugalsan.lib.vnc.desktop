package com.tugalsan.api.random.client.core;

import com.tugalsan.api.shape.client.*;
import java.util.*;
import java.util.stream.*;

public class TGS_RandomDriverUtils {

    public static TGS_ShapeLocation<Integer> nextLoc(Random driver, TGS_ShapeDimension<Integer> boundary) {
        var x = nextInt(driver, 0, boundary.width);
        var y = nextInt(driver, 0, boundary.height);
        return new TGS_ShapeLocation(x, y);
    }

    public static TGS_ShapeLocation<Integer> nextLoc(Random driver, TGS_ShapeRectangle<Integer> boundary) {
        var x = nextInt(driver, boundary.x, boundary.x + boundary.width);
        var y = nextInt(driver, boundary.y, boundary.y + boundary.height);
        return new TGS_ShapeLocation(x, y);
    }

    public static TGS_ShapeRectangle<Integer> nextRect(Random driver, TGS_ShapeDimension<Integer> boundary) {
        return nextRect(driver, new TGS_ShapeRectangle(0, 0, boundary.width, boundary.height));
    }

    public static TGS_ShapeRectangle<Integer> nextRect(Random driver, TGS_ShapeRectangle<Integer> boundaryRect) {
        var loc = nextLoc(driver, boundaryRect);
        var width = nextInt(driver, 0, boundaryRect.width - loc.x);
        var height = nextInt(driver, 0, boundaryRect.height - loc.y);
        return new TGS_ShapeRectangle(loc.x, loc.y, width, height);
    }

    public static float nextFloat(Random driver, float min, float max) {
        return driver.nextFloat() * (max - min) + min;
    }

    public static boolean nextBoolean(Random driver) {
        return driver.nextFloat() > 0.5f;
    }

    public static long nextLong(Random driver, long min, long max) {
        return Math.round(driver.nextFloat() * (max - min) + min);
    }

    public static int nextInt(Random driver, int min, int max) {
        return Math.round(driver.nextFloat() * (max - min) + min);
    }

    public static String nextString(Random driver, int charSize, boolean numberChars, boolean smallChars, boolean bigChars, boolean alphaChars, CharSequence customChars) {
        var alphabet = new StringBuilder();
        if (numberChars) {
            alphabet.append("023456789");//1 is removed for visibility issues
        }
        if (smallChars) {
            alphabet.append("abcdefghijkmnoprstuvwyxz");//l is removed for visibility issues
        }
        if (bigChars) {
            alphabet.append("ABCDEFGHJKLMNOPRSTUVWYXZ");//I is removed for visibility issues
        }
        if (alphaChars) {
            alphabet.append("_-.");//NETWORK SAFE
        }
        if (customChars != null) {
            alphabet.append(customChars);
        }

        return nextString(driver, charSize, alphabet);
    }

    public static String nextString(Random driver, int charSize, CharSequence alphabet) {
        var N = alphabet.length();
        var sb = new StringBuilder(charSize);
        IntStream.range(0, charSize).forEachOrdered(i -> sb.append(alphabet.charAt(driver.nextInt(N))));
        return sb.toString();
    }

    public static int[] nextIntArray(Random driver, int size, int minValue, int maxValue) {
        var r = new int[size];
        if (minValue == maxValue) {
            IntStream.range(0, r.length).parallel().forEach(i -> r[i] = minValue);
        } else {
            var rangeValue = maxValue - minValue;
            IntStream.range(0, r.length).parallel().forEach(i -> r[i] = minValue + (int) (driver.nextFloat() * rangeValue));
        }
        return r;
    }
}
