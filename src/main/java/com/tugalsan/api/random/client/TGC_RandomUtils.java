package com.tugalsan.api.random.client;

import com.tugalsan.api.unsafe.client.TGS_UnSafe;

public class TGC_RandomUtils {

    @Deprecated //NOT IMPLEMENTED
    public static String getUUIDType5(String seed) {
        return TGS_UnSafe.thrwReturns(TGC_RandomUtils.class.getSimpleName(), "getUUIDType5", "not implemented");
    }

    public native static String nextUUIDType4() /*-{
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
            function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r
                        : (r & 0x3 | 0x8);
                return v.toString(16);
            });
}-*/;
}
