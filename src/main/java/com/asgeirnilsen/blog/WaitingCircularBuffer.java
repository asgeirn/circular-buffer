package com.asgeirnilsen.blog;

import java.util.concurrent.atomic.AtomicInteger;

public class WaitingCircularBuffer<T> extends CircularBuffer<T> {

    public WaitingCircularBuffer(int size) {
        super(size);
    }

    @Override
    public T take(AtomicInteger idx) {
        T result;
        int counter = 200;
        while ((result = super.take(idx)) == null) {
            if (counter > 100)
                counter--;
            else if (counter > 0) {
                counter--;
                Thread.yield();
            } else {
                try {
                    Thread.sleep(0, 500000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        return result;
    }


}
