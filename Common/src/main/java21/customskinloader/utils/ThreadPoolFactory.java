package customskinloader.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadPoolFactory {
    private ThreadPoolFactory() {
    }

    public static ExecutorService create(int poolSize, boolean fifo) {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
