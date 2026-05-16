package customskinloader.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadPoolFactory {
    private ThreadPoolFactory() {
    }

    public static ExecutorService create(int poolSize, boolean fifo) {
        poolSize = Math.max(1, poolSize);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, fifo ? new LinkedBlockingQueue<>(poolSize) : new LIFOBlockingQueue<>(new LinkedBlockingDeque<>(poolSize)));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
