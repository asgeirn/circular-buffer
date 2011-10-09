package com.asgeirnilsen.blog;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CircularBufferTest {

    @Test
    public void emptyShouldReturnNull() {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicInteger idx = buf.index();
        assertThat(buf.take(idx), nullValue());
    }

    @Test
    public void emptyShouldDrainEmptyList() throws Exception {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicInteger idx = buf.index();
        List<Integer> result = buf.drain(idx);
        assertThat(result.size(), is(0));
    }

    @Test
    public void twoDrainsYieldPartialResults() throws Exception {
        CircularBuffer<Integer> buf = new CircularBuffer<Integer>(10);
        AtomicInteger idx = buf.index();
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
        AtomicInteger idx = buf.index();
        buf.add(1);
        assumeThat(buf.take(idx), is(1));
        for (int i = 2; i < buf.size()+3; i++)
            buf.add(i);
        List<Integer> result = buf.drain(idx);
        assertThat(result, nullValue());
    }
}
