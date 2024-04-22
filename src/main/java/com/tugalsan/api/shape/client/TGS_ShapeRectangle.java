package com.tugalsan.api.shape.client;

public class TGS_ShapeRectangle<T> {

    public T x;
    public T y;
    public T width;
    public T height;

    public TGS_ShapeRectangle(TGS_ShapeLocation<T> position, TGS_ShapeDimension<T> dimension) {
        this(position.x, position.y, dimension.width, dimension.height);
    }

    public String toString() {
        return TGS_ShapeRectangle.class.getSimpleName() + "{x/y/width/height: " + x + "/" + y + "/" + width + "/" + height + "}";
    }

    public TGS_ShapeRectangle() {
        this(null, null, null, null);
    }

    public TGS_ShapeRectangle(T x, T y, T width, T height) {
        set(x, y, width, height);
    }

    public TGS_ShapeRectangle(TGS_ShapeRectangle<T> position) {
        sniffFrom(position);
    }

    public void sniffFrom(TGS_ShapeRectangle<T> position) {
        this.x = position.x;
        this.y = position.y;
        this.width = position.width;
        this.height = position.height;
    }

    public void set(T x, T y, T width, T height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
