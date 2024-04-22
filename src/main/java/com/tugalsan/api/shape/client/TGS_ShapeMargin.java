package com.tugalsan.api.shape.client;

public class TGS_ShapeMargin<T> {

    public T left;
    public T right;
    public T up;
    public T down;

    public String toString() {
        return TGS_ShapeMargin.class.getSimpleName() + "{left/right/up/down: " + left + "/" + right + "/" + up + "/" + down + "}";
    }

    public TGS_ShapeMargin(T left, T right, T up, T down) {
        set(left, right, up, down);
    }

    public TGS_ShapeMargin(TGS_ShapeMargin<T> sides) {
        sniffFrom(sides);
    }

    public void sniffFrom(TGS_ShapeMargin<T> sides) {
        this.left = sides.left;
        this.right = sides.right;
        this.up = sides.up;
        this.down = sides.down;
    }

    public void set(T left, T right, T up, T down) {
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }
}
