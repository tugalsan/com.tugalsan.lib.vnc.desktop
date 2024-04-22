package com.tugalsan.api.time.client;

import java.util.*;

public class TGC_TimeUtils {

    public static int getOffsetHours() {
        return new Date().getTimezoneOffset() / -60;
    }
}
