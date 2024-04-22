package com.tugalsan.api.shape.client;

public class TGS_ShapeCapacity2D<T> {

    public T rowSize; 
    public T colSize;

    @Override
    public String toString() {
        return TGS_ShapeCapacity2D.class.getSimpleName() + "{rowSize/colSize: " + rowSize + "/" + colSize + "}";
    }

    public TGS_ShapeCapacity2D(T rowSize, T colSize) {
        set(rowSize, colSize);
    }

    public TGS_ShapeCapacity2D(TGS_ShapeCapacity2D<T> capacity) {
        sniffFrom(capacity);
    }

    final public void sniffFrom(TGS_ShapeCapacity2D<T> capacity) {
        this.rowSize = capacity.rowSize;
        this.colSize = capacity.colSize;
    }

    final public void set(T rowSize, T colSize) {
        this.rowSize = rowSize;
        this.colSize = colSize;
    }
}
