package com.tugalsan.api.random.server;

import com.tugalsan.api.random.server.core.TS_UUIDType5Utils;
import com.tugalsan.api.random.client.core.TGS_RandomDriverUtils;
import com.tugalsan.api.hex.client.TGS_HexUtils;
import com.tugalsan.api.shape.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class TS_RandomUtils {

    public static String getUUIDType5(String seed) {
        return TS_UUIDType5Utils.run(seed).toString();
    }

    public static String nextUUIDType4() {
        return TGS_UnSafe.call(() -> {
            var salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
            return TGS_HexUtils.toHex(salt.digest());
        });
    }

    public static final Random driver() {//THREAD SAFE
        return ThreadLocalRandom.current();
    }

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
