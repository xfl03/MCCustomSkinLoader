package customskinloader.utils;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ForwardingBlockingQueue;

public class LIFOBlockingQueue<E> extends ForwardingBlockingQueue<E> {
    private final BlockingDeque<E> deque;

    public LIFOBlockingQueue(BlockingDeque<E> deque) {
        this.deque = deque;
    }

    @Override
    protected BlockingQueue<E> delegate() {
        return deque;
    }

    @Override
    public boolean offer(E e) {
        return deque.offerFirst(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
        deque.putFirst(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return deque.offerFirst(e, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException {
        return deque.takeFirst();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return deque.pollFirst(timeout, unit);
    }

    @Override
    public E peek() {
        return deque.peekFirst();
    }
}
