package com.asgeirnilsen.blog;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class CircularBufferTest {

    @Test
    public void emptyShouldReturnNull() {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicLong idx = buf.index();
        assertThat(buf.take(idx), nullValue());
    }

    @Test
    public void emptyShouldDrainEmptyList() throws Exception {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicLong idx = buf.index();
        List<Integer> result = buf.drain(idx);
        assertThat(result.size(), is(0));
    }

    @Test
    public void twoDrainsYieldPartialResults() throws Exception {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicLong idx = buf.index();
        for (int i = 0; i < 10; i++)
            buf.add(i);
        List<Integer> first = buf.drain(idx);
        assertThat(first, equalTo(Arrays.asList(0,1,2,3,4,5,6,7,8,9)));
        for (int i = 10; i < 20; i++)
            buf.add(i);
        List<Integer> second = buf.drain(idx);
        assertThat(second, equalTo(Arrays.asList(10,11,12,13,14,15,16,17,18,19)));
    }

    @Test
    public void whatHappensAtWraparound() throws Exception {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicLong idx = buf.index();
        buf.add(1);
        assumeThat(buf.take(idx), is(1));
        for (int i = 2; i < buf.size()+3; i++)
            buf.add(i);
        List<Integer> result = buf.drain(idx);
        assertThat(result, nullValue());
    }

    @Test
    public void concurrentProducers() throws Exception {
        final int size = 1024*1024;
        final CircularBuffer<Integer> buffer = new CircularBuffer<Integer>(size);
        AtomicLong index = buffer.index();
        final Queue<Integer> input = new ConcurrentLinkedQueue<Integer>();
        for (int i = 1; i <= size; i++)
            input.add(i);
        assertEquals(size, input.size());
        int parallelism = Runtime.getRuntime().availableProcessors();
        System.out.println("Running " + parallelism + " threads...");
        Set<Callable<Void>> tasks = new HashSet<Callable<Void>>();
        for (int i = 0; i < parallelism; i++)
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (true) {
                        buffer.add(input.remove());
                    }
                }
            });
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        List<Future<Void>> result = executorService.invokeAll(tasks, 30, SECONDS);
        assertEquals(parallelism, result.size());
        for (Future<Void> r : result) {
            assertTrue(r.isDone());
        }
        assertEquals(0, input.size());

        List<Integer> outputList = new ArrayList<Integer>(size);
        SortedSet<Integer> outputSet = new TreeSet<Integer>();
        while (true) {
            Integer ref = buffer.take(index);
            if (ref == null)
                break;
            outputList.add(ref);
            outputSet.add(ref);
        }
        assertEquals(size, outputList.size());
        assertEquals(size, outputSet.size());
        assertEquals(1, (int)outputSet.first());
        assertEquals(size, (int)outputSet.last());
    }

    @Test
    public void concurrentConsumers() throws Exception {
        final int size = 1024*1024;
        final CircularBuffer<Integer> buffer = new CircularBuffer<Integer>(size);
        for (int i = 1; i <= size; i++)
            buffer.add(i);
        final int parallelism = Runtime.getRuntime().availableProcessors();
        System.out.println("Running " + parallelism + " threads...");
        Set<Callable<List<Integer>>> tasks = new HashSet<Callable<List<Integer>>>();
        for (int i = 0; i < parallelism; i++)
            tasks.add(new Callable<List<Integer>>() {
                @Override
                public List<Integer> call() throws Exception {
                    List<Integer> result = new ArrayList<Integer>(size);
                    AtomicLong index = new AtomicLong();
                    while (true) {
                        Integer ref = buffer.take(index);
                        if (ref == null)
                            break;
                        result.add(ref);
                    }
                    return result;
                }
            });
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        List<Future<List<Integer>>> result = executorService.invokeAll(tasks, 30, SECONDS);
        assertEquals(parallelism, result.size());
        for (Future<List<Integer>> f : result) {
            assertTrue(f.isDone());
            assertEquals(size, f.get().size());
        }
    }

}
