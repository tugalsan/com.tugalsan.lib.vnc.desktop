package com.tugalsan.api.shape.client;

public class TGS_ShapeLocation<T> {

    public T x;
    public T y;

    @Override
    public String toString() {
        return TGS_ShapeLocation.class.getSimpleName() + "{x/y: " + x + "/" + y + "}";
    }

    public TGS_ShapeLocation(T x, T y) {
        set(x, y);
    }

    public TGS_ShapeLocation(TGS_ShapeLocation<T> position) {
        sniffFrom(position);
    }

    final public void sniffFrom(TGS_ShapeLocation<T> position) {
        this.x = position.x;
        this.y = position.y;
    }

    final public void set(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public static TGS_ShapeLocation<Double>[] of(double... pairedValues) {
        TGS_ShapeLocation<Double>[] locs = new TGS_ShapeLocation[pairedValues.length];
        var offset = 0;
        for (var i = 0; i < pairedValues.length; i += 2) {
            var loc = new TGS_ShapeLocation<Double>(pairedValues[i], pairedValues[i + 1]);
            locs[offset] = loc;
            offset++;
        }
        return locs;
    }

    public static TGS_ShapeLocation<Float>[] of(float... pairedValues) {
        TGS_ShapeLocation<Float>[] locs = new TGS_ShapeLocation[pairedValues.length];
        var offset = 0;
        for (var i = 0; i < pairedValues.length; i += 2) {
            var loc = new TGS_ShapeLocation<Float>(pairedValues[i], pairedValues[i + 1]);
            locs[offset] = loc;
            offset++;
        }
        return locs;
    }

    public static TGS_ShapeLocation<Integer>[] of(int... pairedValues) {
        TGS_ShapeLocation<Integer>[] locs = new TGS_ShapeLocation[pairedValues.length];
        var offset = 0;
        for (var i = 0; i < pairedValues.length; i += 2) {
            var loc = new TGS_ShapeLocation<Integer>(pairedValues[i], pairedValues[i + 1]);
            locs[offset] = loc;
            offset++;
        }
        return locs;
    }
}
