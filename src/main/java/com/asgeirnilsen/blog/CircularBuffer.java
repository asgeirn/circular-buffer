package com.asgeirnilsen.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A lock-free thread safe circular fixed length buffer.
 *
 * Uses an AtomicInteger as index counter and an AtomicReferenceArray to hold the references to the values.
 *
 * When the buffer is full, the oldest item is overwritten.
 *
 */
public class CircularBuffer<T> {

    private final AtomicInteger index = new AtomicInteger(-1);
    private final AtomicReferenceArray<T> buffer;
    private final int size;
    private final int mask;

    public CircularBuffer(int size) {
        assert size > 0 : "Size must be positive";
        size = size - 1;
        size = size | (size >> 1);
        size = size | (size >> 2);
        size = size | (size >> 4);
        size = size | (size >> 8);
        size = size | (size >> 16);
        size = size + 1;
        this.size = size;
        this.mask = size - 1;
        buffer = new AtomicReferenceArray<T>(this.size);
    }

    public int size() {
        return size;
    }

    public void add(T item) {
        assert item != null : "Item must be non-null";
        buffer.set(index.incrementAndGet() & mask, item);
    }

    public T get(int i) {
        return buffer.get(i & mask);
    }

    public T take(AtomicInteger idx) {
        if (idx.get() >= index.get())
            return null;
        if (index.get() - idx.get() > size) {
            idx.lazySet(index.get());
            return null;
        }
        return buffer.get(idx.incrementAndGet() & mask);
    }

    public List<T> drain(AtomicInteger idx) {
        if (index.get() - idx.get() > size) {
            idx.set(index.get());
            return null;
        }
        List<T> result = new ArrayList<T>(index.get()-idx.get());
        while (idx.get() < index.get())
            result.add(buffer.get(idx.incrementAndGet() & mask));
        return result;
    }

    public AtomicInteger index() {
        return new AtomicInteger(index.get());
    }
}
