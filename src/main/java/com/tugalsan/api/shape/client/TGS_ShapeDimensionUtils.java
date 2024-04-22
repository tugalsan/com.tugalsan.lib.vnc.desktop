package com.tugalsan.api.shape.client;

import java.util.List;

public class TGS_ShapeDimensionUtils {

    public static TGS_ShapeDimension<Integer> getDimension(TGS_ShapeRectangle<Integer> rect) {
        return new TGS_ShapeDimension(rect.width, rect.height);
    }

    public static TGS_ShapeDimension<Integer> getDimension(int radius) {
        return new TGS_ShapeDimension(radius, radius);
    }

    public static TGS_ShapeDimension<Integer> getDimension(List<TGS_ShapeLocation<Integer>> locs) {
        if (locs.isEmpty() || locs.size() == 1) {
            return new TGS_ShapeDimension(0, 0);
        }
        var minX = locs.stream().parallel().mapToInt(loc -> loc.x).min().orElse(0);
        var minY = locs.stream().parallel().mapToInt(loc -> loc.y).min().orElse(0);
        var makX = locs.stream().parallel().mapToInt(loc -> loc.x).min().orElse(0);
        var makY = locs.stream().parallel().mapToInt(loc -> loc.y).max().orElse(0);
        return new TGS_ShapeDimension(
                makX - minX,
                makY - minY
        );
    }
}
