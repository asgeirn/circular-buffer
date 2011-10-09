package com.asgeirnilsen.blog;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

public class LockingCircularBufferTest {

    @Test
    public void testWait() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CircularBuffer<Integer> buffer = new LockingCircularBuffer<Integer>(2, new ReentrantLock());
        final AtomicInteger i = buffer.index();
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

}
