package com.tugalsan.api.list.client;

import java.util.*;
import java.util.stream.*;
import com.tugalsan.api.time.client.*;
import com.tugalsan.api.cast.client.*;
import com.tugalsan.api.runnable.client.*;
import com.tugalsan.api.shape.client.*;
import com.tugalsan.api.stream.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.validator.client.*;

public class TGS_ListTable {

    @Deprecated
    public TGS_ListTable(boolean setNullAsEmptyString) {
        rows = TGS_ListUtils.of();
        this.setNullAsEmptyString = setNullAsEmptyString;
    }
    private boolean setNullAsEmptyString;

    protected TGS_ListTable(List list, boolean setNullAsEmptyString) {
        this(setNullAsEmptyString);
        IntStream.range(0, list.size()).forEachOrdered(ri -> {
            setValue(ri, 0, list.get(ri));
        });
    }

    public static TGS_ListTable of(boolean setNullAsEmptyString) {
        return new TGS_ListTable(setNullAsEmptyString);
    }

    public static TGS_ListTable ofStr() {
        return new TGS_ListTable(true);
    }

    public static TGS_ListTable of(List list, boolean setNullAsEmptyString) {
        return new TGS_ListTable(list, setNullAsEmptyString);
    }

    @Deprecated
    public static TGS_ListTable of(List list) {
        return new TGS_ListTable(list, true);
    }

    public int colIdxByHeader(String text) {
        return IntStream.range(0, getColumnSize(0))
                .filter(ci -> Objects.equals(getValueAsString(0, ci), text))
                .findAny().orElse(-1);
    }

    public TGS_ListTable forEachRowIdx(TGS_ValidatorType1<Integer> optionalRowIdxValidator, TGS_RunnableType1<Integer> ri) {
        TGS_StreamUtils.forward(0, getRowSize()).forEachOrdered(rowIdx -> {
            if (optionalRowIdxValidator != null && !optionalRowIdxValidator.validate(rowIdx)) {
                return;
            }
            ri.run(rowIdx);
        });
        return this;
    }

    public TGS_ListTable forEachRowIdxReversed(TGS_ValidatorType1<Integer> optionalRowIdxValidator, TGS_RunnableType1<Integer> ri) {
        TGS_StreamUtils.reverse(0, getRowSize()).forEachOrdered(rowIdx -> {
            if (optionalRowIdxValidator != null && !optionalRowIdxValidator.validate(rowIdx)) {
                return;
            }
            ri.run(rowIdx);
        });
        return this;
    }

    public void deleteColumnToRight(int fromColIdx) {
        IntStream.range(0, getRowSize()).forEachOrdered(i -> {
            var a = getRow(i);
            while (a.size() > fromColIdx) {
                a.remove(fromColIdx);
            }
        });
    }

    public void deleteRowToDown(int fromRowIdx) {
        while (getRowSize() > fromRowIdx) {
            TGS_ListTable.this.deleteRow(getRowSize() - 1);
        }
    }

    public void deleteRow(TGS_ValidatorType1<Integer> ri, TGS_RunnableType1<Integer> runAfterEveryDeleteRowAction) {
        TGS_StreamUtils.reverse(0, getRowSize()).forEachOrdered(_ri -> {
            if (ri.validate(_ri)) {
                TGS_ListTable.this.deleteRow(_ri);
                if (runAfterEveryDeleteRowAction != null) {
                    runAfterEveryDeleteRowAction.run(_ri);
                }
            }
        });
    }

    public void deleteRow(TGS_ValidatorType1<Integer> ri) {
        deleteRow(ri, null);
    }

    public void addHeaders(List<String> headers, boolean headerBold) {
        insertEmptyRow(0);
        IntStream.range(0, headers.size()).forEachOrdered(ci -> setValue(0, ci, headers.get(ci)));
        setHeaderBold(headerBold);
    }

    public void addHeaderAsColumnNumber() {
        insertEmptyRow(0);
        IntStream.range(0, getMaxColumnSize()).forEachOrdered(ci -> setValue(0, ci, String.valueOf(ci)));
    }

    public TGS_ListTable sortColumnAsDouble(int columnIndex, boolean skipHeader) {
        var rs = rows.size();
        List tmpRow;
        String is, js;
        Double it, jt;
        for (var ri = 0 + (skipHeader ? 1 : 0); ri < rs; ri++) {
            is = getValueAsString(ri, columnIndex);
            if (is.startsWith("%")) {
                is = is.substring(1);
            }
            it = TGS_CastUtils.toDouble(is);
            if (it == null) {
                continue;
            }
            for (var j = ri + 1; j < rs; j++) {
                js = getValueAsString(j, columnIndex);
                if (js.startsWith("%")) {
                    js = js.substring(1);
                }
                jt = TGS_CastUtils.toDouble(js);
                if (jt == null) {
                    continue;
                }
                if (it.compareTo(jt) > 0) {
                    tmpRow = getRow(ri);
                    rows.remove(ri);
                    rows.add(tmpRow);
                    ri = -1;
                    break;
                }
            }
        }
        return this;
    }

    @Deprecated //Untested
    public TGS_ListTable sortColumnAsInteger(int columnIndex, boolean skipHeader) {
        var rs = rows.size();
        for (var ri = skipHeader ? 1 : 0; ri < rs; ri++) {
            var is = getValueAsString(ri, columnIndex);
            if (is.startsWith("%")) {
                is = is.substring(1);
            }
            var it = TGS_CastUtils.toInteger(is);
            if (it == null) {
                continue;
            }
            for (var j = ri + 1; j < rs; j++) {
                var js = getValueAsString(j, columnIndex);
                if (js.startsWith("%")) {
                    js = js.substring(1);
                }
                var jt = TGS_CastUtils.toInteger(js);
                if (jt == null) {
                    continue;
                }
                if (it.compareTo(jt) > 0) {
                    var tmpRow = getRow(ri);
                    rows.remove(ri);
                    rows.add(tmpRow);
                    ri = -1;
                    break;
                }
            }
        }
        return this;
    }

    public void sortColumnAsString(int ci, boolean skipHeader) {//0, false
        var rs = rows.size();
        var ri = skipHeader ? 1 : 0;
        while (ri < rs) {
            var rj = ri + 1;
            while (rj < rs) {
                if ((getValueAsString(ri, ci)).compareTo(getValueAsString(rj, ci)) > 0) {
                    var temp = getRow(ri);
                    setRow(ri, getRow(rj));
                    setRow(rj, temp);
                }
                rj += 1;
            }
            ri += 1;
        }
    }

    //IS IT WORKING?
    @Deprecated
    public void moveRow(int fromIndex, int toIndex) {
        while (rows.size() - 1 < toIndex) {
            rows.add(TGS_ListUtils.of());
        }
        var o = rows.get(fromIndex);
        rows.remove(fromIndex);
        rows.add(toIndex, o);
    }

    public void addRow_SumInteger() {
        var rs = rows.size();
        IntStream.range(0, getMaxColumnSize()).forEachOrdered(ci -> {
            var sum = 0;
            for (var ri = 0; ri < rs; ri++) {
                var str = getValueAsString(ri, ci);
                var val = TGS_CastUtils.toInteger(str);
                if (val == null) {
                    continue;
                }
                sum += val;
            }
            setValue(rs, ci, sum);
        });
    }

    public void addRow_SumDouble() {
        var rs = rows.size();
        IntStream.range(0, getMaxColumnSize()).forEachOrdered(ci -> {
            var sum = 0d;
            for (var ri = 0; ri < rs; ri++) {
                var str = getValueAsString(ri, ci);
                var val = TGS_CastUtils.toDouble(str);
                if (val == null) {
                    continue;
                }
                sum += val;
            }
            setValue(rs, ci, sum);
        });
    }

    public double calcSumDouble(boolean skipHeaders, int ci, String nullText) {
        var rs = rows.size() - 1;
        var failed = false;
        var i_sum = 0d;
        for (var ri = skipHeaders ? 1 : 0; ri < rs; ri++) {
            var s = getValueAsString(ri, ci);
            var i = TGS_CastUtils.toDouble(s);
            if (i == null) {
                failed = true;
                break;
            }
            i_sum += i;
        }
        setValue(rs, ci, failed ? nullText : i_sum);
        return i_sum;
    }

    public int calcSumInteger(boolean skipHeaders, int ci, String nullText) {
        var rs = rows.size() - 1;
        var failed = false;
        var i_sum = 0;
        for (var ri = skipHeaders ? 1 : 0; ri < rs; ri++) {
            var s = getValueAsString(ri, ci);
            var i = TGS_CastUtils.toInteger(s);
            if (i == null) {
                failed = true;
                break;
            }
            i_sum += i;
        }
        setValue(rs, ci, failed ? nullText : i_sum);
        return i_sum;
    }

    public TGS_ListTable cloneIt() {
        var clone = TGS_ListTable.ofStr();
        clone.sniffRows(this);
        clone.setHeaderBold(this.isHeaderBold());
        return clone;
    }

    public void sniffHeader(TGS_ListTable table) {
        this.headerBold = table.headerBold;
    }

    public void sniffRows(TGS_ListTable table) {
        IntStream.range(0, table.getRowSize()).forEachOrdered(i -> sniffRow(table, i));
    }

    public void sniffRow(TGS_ListTable table, int rowIndex) {
        var newRowIdx = getRowSize();
        IntStream.range(0, table.getColumnSize(rowIndex)).forEachOrdered(i -> {
            setValue(newRowIdx, i, table.getValueAsObject(rowIndex, i));
        });
    }

    public TGS_ListTable insertHeaderBold(List lst) {
        setHeaderBold(true);
        rows.add(0, lst);
        return this;
    }

    public TGS_ListTable insertHeaderBold(Object... values) {
        setHeaderBold(true);
        insertEmptyRow(0);
        setValue(0, values);
        return this;
    }

    boolean headerBold;

    public boolean isHeaderBold() {
        return headerBold;
    }

    public void setHeaderBold(boolean headerBold) {
        this.headerBold = headerBold;
    }

    protected List rows;

    public void deleteRow(int rowIndex) {
        rows.remove(rowIndex);
    }

    public void deleteColumns_sortIdxAsReversed(int... colIdxs) {
        TGS_ListSortUtils.sortPrimativeIntReversed(colIdxs);
        Arrays.stream(colIdxs).forEachOrdered(idx -> {
            deleteColumn(idx);
        });
    }

    public void deleteColumns(IntStream colIdxs) {
        deleteColumns(TGS_StreamUtils.toLst(colIdxs));
    }

    public void deleteColumns(List<Integer> colIdxs) {
        deleteColumns_sortIdxAsReversed(colIdxs.stream().mapToInt(val -> val).toArray());
    }

    public void deleteRows_sortIdxAsReversed(int... rowIdxs) {
        TGS_ListSortUtils.sortPrimativeIntReversed(rowIdxs);
        Arrays.stream(rowIdxs).forEachOrdered(idx -> {
            deleteRow(idx);
        });
    }

    public void deleteRows(List<Integer> rowIdxs) {
        deleteRows_sortIdxAsReversed(rowIdxs.stream().mapToInt(val -> val).toArray());
    }

    public void deleteColumnLast() {
        deleteColumn(getColumnSize(0) - 1);
    }

    public void deleteColumnFirst() {
        deleteColumn(0);
    }

    public void deleteColumn(int columnIndex) {
        var rowSize = rows.size();
        IntStream.range(0, rowSize).forEachOrdered(ri -> {
            if (getColumnSize(ri) > columnIndex) {
                ((List) rows.get(ri)).remove(columnIndex);
            }
        });
    }

    public void clearColumn(int columnIndex) {
        var rowSize = rows.size();
        IntStream.range(0, rowSize).forEachOrdered(ri -> {
            if (getColumnSize(ri) > columnIndex) {
                setValue(ri, columnIndex, "");
            }
        });
    }

    public void copyRowBefore(int fromRowIndex, int insertRowIndex) {
        insertEmptyRow(insertRowIndex);
        if (fromRowIndex > insertRowIndex) {
            fromRowIndex++;
        }
        var final_fromRowIndex = fromRowIndex;
        IntStream.range(0, getColumnSize(fromRowIndex)).forEachOrdered(c -> setValue(insertRowIndex, c, getValueAsObject(final_fromRowIndex, c)));
    }

    public void copyColumnBefore(int fromColumnIndex, int insertColumnIndex) {
        insertEmptyColumn(insertColumnIndex);
        if (fromColumnIndex > insertColumnIndex) {
            fromColumnIndex++;
        }
        var final_fromColumnIndex = fromColumnIndex;
        IntStream.range(0, rows.size()).forEachOrdered(ri -> {
            var csi = getColumnSize(ri);
            if (csi > final_fromColumnIndex) {
                setValue(ri, insertColumnIndex, getValueAsObject(ri, final_fromColumnIndex));
            }
        });
    }

    public void clear() {
        rows.clear();
    }

    public void addRow(List row) {
        rows.add(row);
    }

    public void setValue(int rowIndex, Object... values) {
        if (values.length == 1 && values[0] instanceof Object[] && !(values[0] instanceof byte[])) {
            var valuesArr = (Object[]) values[0];
            IntStream.range(0, valuesArr.length).forEachOrdered(ci -> setValue(rowIndex, ci, values[ci]));
            return;
        }
        IntStream.range(0, values.length).forEachOrdered(ci -> setValue(rowIndex, ci, values[ci]));
    }

    public void setValue(int rowIndex, int columnIndex, Object value) {
        while (rowIndex >= rows.size()) {
            rows.add(TGS_ListUtils.of());
        }
        List row = (List) rows.get(rowIndex);
        while (columnIndex >= row.size()) {
            row.add("");
        }
        row.set(columnIndex, value == null ? "" : (setNullAsEmptyString ? getStringValue(value) : value));
    }

    public List getRows() {
        return rows;
    }

    public List setRows(List rows) {
        return this.rows = rows;
    }

    public List popRow(int rowIndex) {
        var row = getRow(rowIndex);
        deleteRow(rowIndex);
        return row;
    }

    public List getRow_asClonedList(int rowIndex) {
        var raw = getRow(rowIndex);
        if (raw == null) {
            return null;
        }
        var clone = new ArrayList();
        raw.forEach(item -> clone.add(item));
        return clone;
    }

    public List getRow(int rowIndex) {
        if (rowIndex >= rows.size()) {
            return null;
        }
        if (rowIndex < 0) {
            return null;
        }
        return (List) rows.get(rowIndex);
    }

    public List getColumn(int colIndex) {
        if (colIndex >= getMaxColumnSize()) {
            return null;
        }
        if (colIndex < 0) {
            return null;
        }
        List col = TGS_ListUtils.of();
        IntStream.range(0, getRowSize()).forEachOrdered(ri -> col.add(getValueAsObject(ri, colIndex)));
        return col;
    }

    public void setRow(int rowIndex, List newRow) {
        while (rowIndex >= rows.size()) {
            rows.add(TGS_ListUtils.of());
        }
        rows.set(rowIndex, newRow);
    }

    public void transposeTo(TGS_ListTable targetTable) {
        targetTable.clear();
        IntStream.range(0, rows.size()).forEachOrdered(ri -> {
            var row = (List) rows.get(ri);
            IntStream.range(0, row.size()).forEachOrdered(ci -> {
                targetTable.setValue(ci, ri, row.get(ci));
            });
        });
    }

    protected String getStringValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof TGS_Time) {//GWT WONT LIKE PATTERN
            ((TGS_Time) value).toString_dateOnly();
        } else if (value instanceof String) {
            return String.valueOf((String) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value).toString();
        } else if (value instanceof Byte) {
            return ((Byte) value).toString();
        } else if (value instanceof Short) {
            return ((Short) value).toString();
        } else if (value instanceof Integer) {
            return ((Integer) value).toString();
        } else if (value instanceof Long) {
            return ((Long) value).toString();
        } else if (value instanceof Float) {
            return ((Float) value).toString();
        } else if (value instanceof Double) {
            return ((Double) value).toString();
        }
        return value.toString();
    }

    public Object getValueAsObject(int rowIndex, int columnIndex) {
        if (rowIndex >= rows.size()) {
            return null;
        }
        List v = (List) rows.get(rowIndex);
        if (columnIndex >= v.size()) {
            return null;
        }
        return v.get(columnIndex);
    }

    public String getValueAsString(int rowIndex, int columnIndex) {
        return String.valueOf(getValueAsObject(rowIndex, columnIndex));
    }

    public boolean isValueEmpty(int rowIndex, int columnIndex) {
        return TGS_StringUtils.isNullOrEmpty(getValueAsString(rowIndex, columnIndex));
    }

    public boolean isValuePresent(int rowIndex, int columnIndex) {
        return !isValueEmpty(rowIndex, columnIndex);
    }

    public Integer getValueAsInteger(int rowIndex, int columnIndex) {
        return TGS_CastUtils.toInteger(getValueAsString(rowIndex, columnIndex));
    }

    public Long getValueAsLong(int rowIndex, int columnIndex) {
        return TGS_CastUtils.toLong(getValueAsString(rowIndex, columnIndex));
    }

    public Double getValueAsDouble(int rowIndex, int columnIndex) {
        return TGS_CastUtils.toDouble(getValueAsString(rowIndex, columnIndex));
    }

    public Boolean getValueAsBoolean(int rowIndex, int columnIndex) {
        return TGS_CastUtils.toBoolean(getValueAsString(rowIndex, columnIndex));
    }

    public Double multiplyValueAsDouble(int rowIndex, int columnIndex, double factor) {
        var val = getValueAsDouble(rowIndex, columnIndex);
        if (val == null) {
            return null;
        }
        val *= factor;
        setValue(rowIndex, columnIndex, val);
        return val;
    }

    public Integer multiplyValueAsInteger(int rowIndex, int columnIndex, int factor) {
        var val = getValueAsInteger(rowIndex, columnIndex);
        if (val == null) {
            return null;
        }
        val *= factor;
        setValue(rowIndex, columnIndex, val);
        return val;
    }

    public void multiplyColumnAsDouble(int columnIndex, double factor, Integer skipHeaderRows) {
        IntStream.range(skipHeaderRows == null ? 0 : skipHeaderRows, getRowSize()).forEachOrdered(ri -> {
            multiplyValueAsDouble(ri, columnIndex, factor);
        });
    }

    public void multiplyColumnAsInteger(int columnIndex, Integer factor, Integer skipHeaderRows) {
        IntStream.range(skipHeaderRows == null ? 0 : skipHeaderRows, getRowSize()).forEachOrdered(ri -> {
            multiplyValueAsInteger(ri, columnIndex, factor);
        });
    }

    public int getRowSize() {
        return rows.size();
    }

    public int getColumnSize(int rowIndex) {
        if (rowIndex >= rows.size()) {
            return 0;
        }
        List v = (List) rows.get(rowIndex);
        return v.size();
    }

    public void clearValue(int rowIndex, int columnIndex) {
        setValue(rowIndex, columnIndex, null);
    }

    public void setRangeValue(int fromRowIndex, int fromColumnIndex, int toRowIndex, int toColumnIndex, Object value) {
        for (var i = fromRowIndex; i <= toRowIndex; i++) {
            for (var j = fromColumnIndex; j <= toColumnIndex; j++) {
                setValue(i, j, value);
            }
        }
    }

    public void clearRange(int fromRowIndex, int fromColumnIndex, int toRowIndex, int toColumnIndex) {
        setRangeValue(fromRowIndex, fromColumnIndex, toRowIndex, toColumnIndex, null);
    }

    public int getMaxColumnSize() {
        var maxColumnSize = 0;
        for (var ri = 0; ri < getRowSize(); ri++) {
            if (getColumnSize(ri) > maxColumnSize) {
                maxColumnSize = getColumnSize(ri);
            }
        }
        return maxColumnSize;
    }

    @Override
    public String toString() {
        List<String> a = TGS_ListUtils.of();
        rows.forEach(ai -> a.add(((List) ai).toString()));
        return TGS_StringUtils.toString_ln(a);
    }

    public void insertRow(int rowIndex, List newRow) {
        insertEmptyRow(rowIndex);
        setRow(rowIndex, newRow);
    }

    public void insertEmptyRow(int rowIndex) {
        if (rowIndex < 0) {
            return;
        }
        if (rowIndex == rows.size()) {
            rows.add(TGS_ListUtils.of());
            return;
        }
        if (rowIndex < rows.size()) {
            rows.add(rowIndex, TGS_ListUtils.of());
        }
    }

    public void insertEmptyColumn(int columnIndex) {
        if (columnIndex < 0) {
            return;
        }
        IntStream.range(0, getRowSize()).forEach(ri -> {
            TGS_StreamUtils.reverse(columnIndex, getColumnSize(ri)).forEach(ci -> {
                setValue(ri, ci + 1, getValueAsObject(ri, ci));
                setValue(ri, ci, null);
            });
        });
    }

    public void setRowSize(int newMaxSize) {
        var rs = rows.size();
        TGS_StreamUtils.reverse(0, newMaxSize)
                .filter(i -> i < rs)
                .forEach(i -> rows.remove(i));
    }

    public TGS_ShapeLocation<Integer> find(TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, rows.size())
                .mapToObj(ri -> TGS_ShapeLocation.of(ri, findAnyColIdx(ri, validator))[0])
                .filter(p2 -> p2.y != -1)
                .findAny().orElse(null);
    }

    public int findAnyColIdx(int atRowIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, getColumnSize(atRowIdx))
                .filter(ci -> validator.validate(getValueAsObject(atRowIdx, ci)))
                .findAny().orElse(-1);
    }

    public int findFirstColIdx(int atRowIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, getColumnSize(atRowIdx))
                .filter(ci -> validator.validate(getValueAsObject(atRowIdx, ci)))
                .findFirst().orElse(-1);
    }

    public List<Integer> findAllColIdxs(int atRowIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, getColumnSize(atRowIdx))
                .filter(ci -> validator.validate(getValueAsObject(atRowIdx, ci)))
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int findAnyRowIdx(int atColIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, rows.size())
                .filter(ri -> validator.validate(getValueAsObject(ri, atColIdx)))
                .findAny().orElse(-1);
    }

    public int findFirstRowIdx(int atColIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, rows.size())
                .filter(ri -> validator.validate(getValueAsObject(ri, atColIdx)))
                .findFirst().orElse(-1);
    }

    public List<Integer> findAllRowIdxs(int atColIdx, TGS_ValidatorType1<Object> validator) {
        return IntStream.range(0, rows.size())
                .filter(ri -> validator.validate(getValueAsObject(ri, atColIdx)))
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isEmpty() {
        return getRowSize() == 0;
    }

    public boolean isPresent() {
        return !isEmpty();
    }
}
