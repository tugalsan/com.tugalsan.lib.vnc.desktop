package com.tugalsan.api.shape.client;

public class TGS_ShapeDimension<T> {

    public T width;
    public T height;

    public String toString() {
        return TGS_ShapeDimension.class.getSimpleName() + "{width/height: " + width + "/" + height + "}";
    }

    public TGS_ShapeDimension(T width, T height) {
        set(width, height);
    }

    public TGS_ShapeDimension(TGS_ShapeDimension<T> dimension) {
        sniffFrom(dimension);
    }

    public TGS_ShapeDimension<T> sniffFrom(TGS_ShapeDimension<T> dimension) {
        this.width = dimension.width;
        this.height = dimension.height;
        return this;
    }

    public TGS_ShapeDimension<T> set(T width, T height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public TGS_ShapeDimension<T> cloneIt() {
        return new TGS_ShapeDimension(width, height);
    }
}
