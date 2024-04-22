package com.tugalsan.api.input.server;

import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.awt.Robot;

public class TS_InputCommonUtils {

    public static Robot robot() {
        if (robot != null) {
            return robot;
        }
        return TGS_UnSafe.call(() -> new Robot(), e -> null);
    }
    private static volatile Robot robot = null;
}
