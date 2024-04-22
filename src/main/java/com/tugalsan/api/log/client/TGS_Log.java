package com.tugalsan.api.log.client;

import java.util.*;

public class TGS_Log {

    final public static List<String> FULL_NAMED_CLASSES = new ArrayList(Arrays.asList(
            "ServletContextListener",
            "EntryPoint"
    ));

    public static boolean isFullNamed(Class clazz) {
        return FULL_NAMED_CLASSES.stream().filter(cn -> clazz.getSimpleName().equals(cn)).findAny().isPresent();
    }

    public static int TYPE_LNK() {
        return 0;
    }

    public static int TYPE_INF() {
        return 2;
    }

    public static int TYPE_RES() {
        return 3;
    }

    public static int TYPE_THR() {
        return 4;
    }

    public static int TYPE_ERR() {
        return 5;
    }
}
