package com.asgeirnilsen.blog;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

public class LockingCircularBufferTest {

    @Test
    public void testWait() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CircularBuffer<Integer> buffer = new LockingCircularBuffer<Integer>(2, new ReentrantLock(true));
        final AtomicLong i = buffer.index();
        Future<Integer> result = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return buffer.take(i);
            }
        });
        MILLISECONDS.sleep(500);
        buffer.add(42);
        assertThat(result.get(1, SECONDS), is(42));
    }

    @Test
    public void manyReaders() throws Exception {
        final int size = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*10);
        final CircularBuffer<Integer> buffer = new LockingCircularBuffer<Integer>(2, new ReentrantLock(true));

        final List<Callable<Integer>> callables = new ArrayList<Callable<Integer>>(size);
        callables.add(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                MILLISECONDS.sleep(500);
                buffer.add(42);
                return 42;
            }
        });
        for (int i = 0; i < size; i++) {
            callables.add(new Callable<Integer>() {
                final AtomicLong idx = buffer.index();

                @Override
                public Integer call() throws Exception {
                    return buffer.take(idx);
                }
            });
        }

        final List<Future<Integer>> results = executor.invokeAll(callables, 60, SECONDS);
        for (Future<Integer> f : results) {
            assertThat(f.isCancelled(), is(false));
            assertThat(f.get(), is(42));
        }
    }
}
