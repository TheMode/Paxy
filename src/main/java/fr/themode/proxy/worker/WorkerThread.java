package fr.themode.proxy.worker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WorkerThread extends Thread {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private WorkerThread(Runnable runnable) {
        super(null, runnable, "worker-" + COUNTER.getAndIncrement());
    }

    protected static void start(Consumer<WorkerContext> runnable) {
        new WorkerThread(() -> {
            WorkerContext workerContext = new WorkerContext();
            while (true) {
                try {
                    runnable.accept(workerContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
