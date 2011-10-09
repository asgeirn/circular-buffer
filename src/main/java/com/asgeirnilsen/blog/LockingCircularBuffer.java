package com.asgeirnilsen.blog;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class LockingCircularBuffer<T> extends CircularBuffer<T> {

    private final Lock lock;
    private final Condition condition;

    public LockingCircularBuffer(int size, Lock lock) {
        super(size);
        this.lock = lock;
        condition = lock.newCondition();
    }

    @Override
    public void add(T item) {
        super.add(item);
        condition.signalAll();
    }

    @Override
    public T take(AtomicInteger idx) {
        T result;
        while ((result = super.take(idx)) == null)
            try {
                lock.lock();
                condition.await();
            } catch (InterruptedException e) {
                // ignored
            } finally {
                lock.unlock();
            }
        return result;
    }



}
