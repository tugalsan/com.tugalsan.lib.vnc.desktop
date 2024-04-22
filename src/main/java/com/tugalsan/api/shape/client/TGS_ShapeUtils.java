package com.tugalsan.api.shape.client;

public class TGS_ShapeUtils {

    public static void scale(TGS_ShapeLocation<Float> me, float k) {
        me.x *= k;
        me.y *= k;
    }

    public static TGS_ShapeLocation<Float> scaleAs(TGS_ShapeLocation<Float> org, float k) {
        return new TGS_ShapeLocation(org.x * k, org.y * k);
    }
}
