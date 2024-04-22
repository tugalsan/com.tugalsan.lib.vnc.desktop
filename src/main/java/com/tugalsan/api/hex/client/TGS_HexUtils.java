package com.tugalsan.api.hex.client;

import java.util.*;
import java.util.stream.*;

public class TGS_HexUtils {

    private static String dictionary() {
        return "0123456789ABCDEF";
    }

    public static String toHex(int value_0_255) {
        char[] dictionary = dictionary().toCharArray();
        var sb = new StringBuilder();
        sb.append(dictionary[(value_0_255 - value_0_255 % 16) / 16]);
        sb.append(dictionary[value_0_255 % 16]);
        return sb.toString();
    }

    public static String toHex(int[] values_0_255) {
        var sb = new StringBuilder(values_0_255.length * 2);
        IntStream.range(0, values_0_255.length).forEachOrdered(i -> sb.append(toHex(values_0_255[i])));
        return sb.toString().toUpperCase();//NO TURKISH FIX NEEDED
    }

    public static int toInt(char hexChar1, char hexChar0) {
        return Integer.parseInt(new StringBuilder().append(hexChar1).append(hexChar0).toString(), 16);
    }

    public static int[] toInt(CharSequence hex_withPairChars) {
        var b = new int[hex_withPairChars.length() / 2];
        IntStream.range(0, b.length).parallel().forEach(i -> {
            var index = i * 2;
            b[i] = TGS_HexUtils.toInt(hex_withPairChars.charAt(index), hex_withPairChars.charAt(index + 1));
        });
        return b;
    }

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        char[] dictionary = dictionary().toCharArray();
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = dictionary[v >>> 4];
            hexChars[j * 2 + 1] = dictionary[v & 0x0F];
        }
        return new String(hexChars);
    }
}
