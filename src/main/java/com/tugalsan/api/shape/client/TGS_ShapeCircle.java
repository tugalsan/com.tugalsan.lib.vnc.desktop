package com.tugalsan.api.shape.client;

public class TGS_ShapeCircle<T, Y> {

    public T x;
    public T y;
    public Y radius;

    public TGS_ShapeCircle(TGS_ShapeLocation<T> position, Y radius) {
        this(position.x, position.y, radius);
    }

    @Override
    public String toString() {
        return TGS_ShapeCircle.class.getSimpleName() + "{x/y/radius: " + x + "/" + y + "/" + radius + "}";
    }

    public TGS_ShapeCircle() {
        this(null, null, null);
    }

    public TGS_ShapeCircle(T x, T y, Y radius) {
        set(x, y, radius);
    }

    final public void set(T x, T y, Y radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
}
