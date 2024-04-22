package com.tugalsan.api.list.client;

import java.util.List;
import java.util.stream.IntStream;

public class TGS_ListMatrixUtils {

    public static Object[][] transpose(Object[][] inputArray) {
        if (inputArray == null) {
            return null;
        }
        var transposeO = new Object[inputArray[0].length][inputArray.length];
        IntStream.range(0, inputArray.length).forEachOrdered(i -> IntStream.range(0, inputArray[0].length).forEachOrdered(j -> transposeO[j][i] = inputArray[i][j]));
        return transposeO;
    }

    public static Object[] getColumn(Object[][] inputArray, int columnIndex) {
        if (inputArray == null) {
            return null;
        }
        var colData = new Object[inputArray[0].length];
        IntStream.range(0, inputArray[0].length).parallel().forEach(i -> colData[i] = inputArray[i][columnIndex]);
        return colData;
    }

    public static <T> List<List<T>> transpose(List<List<T>> inputMatrix) {
        List<List<T>> transposedMatrix = TGS_ListUtils.of();
        if (inputMatrix.isEmpty()) {
            return transposedMatrix;
        }
        IntStream.range(0, inputMatrix.get(0).size()).forEachOrdered(ci -> {
            List<T> col = TGS_ListUtils.of();
            IntStream.range(0, inputMatrix.size()).forEachOrdered(ri -> {
                col.add(inputMatrix.get(ri).get(ci));
            });
            transposedMatrix.add(col);
        });
        return transposedMatrix;
    }
}
