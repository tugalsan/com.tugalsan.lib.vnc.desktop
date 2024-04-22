package com.tugalsan.api.random.client;

import com.tugalsan.api.random.client.core.TGS_RandomDriverUtils;
import com.tugalsan.api.shape.client.*;
import java.util.*;

public class TGS_RandomUtils {

    public static final Random driver() {
        if (driver == null) {
            driver = new Random();
        }
        return driver;
    }
    private static volatile Random driver;

    public static TGS_ShapeLocation<Integer> nextLoc(TGS_ShapeDimension<Integer> boundary) {
        return TGS_RandomDriverUtils.nextLoc(driver(), boundary);
    }

    public static TGS_ShapeLocation<Integer> nextLoc(TGS_ShapeRectangle<Integer> boundary) {
        return TGS_RandomDriverUtils.nextLoc(driver(), boundary);
    }

    public static TGS_ShapeRectangle<Integer> nextRect(TGS_ShapeDimension<Integer> boundary) {
        return TGS_RandomDriverUtils.nextRect(driver(), new TGS_ShapeRectangle(0, 0, boundary.width, boundary.height));
    }

    public static TGS_ShapeRectangle<Integer> nextRect(TGS_ShapeRectangle<Integer> boundaryRect) {
        return TGS_RandomDriverUtils.nextRect(driver(), new TGS_ShapeRectangle(0, 0, boundaryRect.width, boundaryRect.height));
    }

    public static float nextFloat(float min, float max) {
        return TGS_RandomDriverUtils.nextFloat(driver(), min, max);
    }

    public static boolean nextBoolean() {
        return TGS_RandomDriverUtils.nextBoolean(driver());
    }

    public static long nextLong(long min, long max) {
        return TGS_RandomDriverUtils.nextLong(driver(), min, max);
    }

    public static int nextInt(int min, int max) {
        return TGS_RandomDriverUtils.nextInt(driver(), min, max);
    }

    public static String nextString(int charSize, boolean numberChars, boolean smallChars, boolean bigChars, boolean alphaChars, CharSequence customChars) {
        return TGS_RandomDriverUtils.nextString(driver(), charSize, numberChars, smallChars, bigChars, alphaChars, customChars);
    }

    public static String nextString(int charSize, CharSequence alphabet) {
        return TGS_RandomDriverUtils.nextString(driver(), charSize, alphabet);
    }

    public static int[] nextIntArray(int size, int minValue, int maxValue) {
        return TGS_RandomDriverUtils.nextIntArray(driver(), size, minValue, maxValue);
    }
}
