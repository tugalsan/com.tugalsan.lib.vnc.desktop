package com.tugalsan.api.list.client;

import java.util.*;
import java.util.stream.*;

public class TGS_ListSortedDistinct<T> {

    final public TreeSet<T> driver = new TreeSet();

    public TGS_ListSortedDistinct<T> clear() {
        driver.clear();
        return this;
    }

    public boolean isEmpty() {
        return driver.isEmpty();
    }

    public boolean isPresent() {
        return !driver.isEmpty();
    }

    public boolean contains(T item) {
        return driver.contains(item);
    }

    public int size() {
        return driver.size();
    }

    public TGS_ListSortedDistinct<T> add(T item) {
        driver.add(item);
        return this;
    }

    public T get(int idx) {
        return driver.stream().skip(idx).findFirst().orElse(null);
    }

    public TGS_ListSortedDistinct<T> removeItem(T item) {
        driver.remove(item);
        return this;
    }

    public Stream<T> stream(boolean parallel) {
        return parallel ? driver.parallelStream() : driver.stream();
    }

    public List<T> toList() {
        return new ArrayList(driver);
    }
}
