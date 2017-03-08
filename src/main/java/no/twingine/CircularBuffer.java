package no.twingine;

import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A lock-free thread safe circular fixed length buffer.
 *
 * Uses an AtomicLong as index counter and an AtomicReferenceArray to hold the references to the values.
 *
 * When the buffer is full, the oldest item is overwritten.
 *
 */
public class CircularBuffer<T> {

    private final AtomicLong index = new AtomicLong(0);
    private final AtomicReferenceArray<T> buffer;
    private final int size;

    public CircularBuffer(int size) {
        assert size > 0 : "Size must be positive";
        this.size = size;
        buffer = new AtomicReferenceArray<T>(this.size);
    }

    public int size() {
        return size;
    }

    public void add(T item) {
        assert item != null : "Item must be non-null";
        buffer.set((int) (index.getAndIncrement() % size), item);
    }

    public T get(long i) {
        return buffer.get((int) (i % size));
    }

    public T take(AtomicLong idx) {
        if (idx.get() >= index.get())
            return null;
        if (index.get() - idx.get() > size) {
            idx.lazySet(index.get());
            throw new BufferOverflowException();
        }
        return get(idx.getAndIncrement());
    }

    public List<T> drain(AtomicLong idx) {
        if (index.get() - idx.get() > size) {
            idx.set(index.get());
            throw new BufferOverflowException();
        }
        List<T> result = new ArrayList<T>((int) (index.get()-idx.get()));
        while (idx.get() < index.get())
            result.add(get(idx.getAndIncrement()));
        return result;
    }

    public AtomicLong index() {
        return new AtomicLong(index.get());
    }
}
