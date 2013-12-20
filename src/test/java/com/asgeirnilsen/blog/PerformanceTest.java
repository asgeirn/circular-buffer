package com.asgeirnilsen.blog;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PerformanceTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { new CircularBuffer<Integer>(32) },
                { new WaitingCircularBuffer<Integer>(32) },
                { new LockingCircularBuffer<Integer>(32, new ReentrantLock()) } });
    }

    private final CircularBuffer<Integer> buffer;

    public PerformanceTest(CircularBuffer<Integer> buffer) {
        super();
        this.buffer = buffer;
    }

    @Test
    public void performanceTest() throws Exception {
        final AtomicInteger produced = new AtomicInteger();
        final AtomicInteger consumed = new AtomicInteger();
        final AtomicLong index = buffer.index();
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        @SuppressWarnings("unchecked")
        List<Callable<Void>> tasks = Arrays.asList(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int i = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    buffer.add(++i);
                    produced.lazySet(i);
                }
                return null;
            }
        }, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                    Integer result = buffer.take(index);
                    if (result != null) {
                        consumed.lazySet(result);
                    }
                }
                return null;
            }
        });
        executorService.invokeAll(tasks, 30, SECONDS);
        System.out.printf("%s, produced: %,d, consumed: %,d%n", buffer.getClass().getSimpleName(), produced.get(), consumed.get());
    }

}
