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

    public CircularBuffer(int size) {
        assert size > 0 : "Size must be positive";
        this.size = size;
        buffer = new AtomicReferenceArray<T>(this.size);
    }

    public void add(T item) {
        if (index.compareAndSet(size - 1, 0)) {
            buffer.set(0, item);
        } else {
            buffer.set(index.incrementAndGet(), item);
        }
    }

    public T get(int i) {
        return buffer.get(i);
    }

    public T take(AtomicInteger idx) {
        int i = index.get();
        if (idx.compareAndSet(i, i))
            return null;
        if (idx.compareAndSet(size - 1, 0))
            return buffer.get(0);
        else
            return buffer.get(idx.incrementAndGet());
    }

    public List<T> drain(AtomicInteger idx) {
        List<T> result = new ArrayList<T>();
        int i = index.get();
        while (!idx.compareAndSet(i, i)) {
            result.add(take(idx));
            i = index.get();
        }
        return result;
    }

    public AtomicInteger index() {
        return new AtomicInteger(index.get());
    }
}
