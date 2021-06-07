package fr.themode.proxy.network;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WorkerThread extends Thread {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private WorkerThread(Runnable runnable) {
        super(null, runnable, "worker-" + COUNTER.getAndIncrement());
    }

    protected static WorkerThread start(Consumer<WorkerContext> runnable) {
        WorkerThread thread = new WorkerThread(() -> {
            WorkerContext workerContext = new WorkerContext();
            while (true) {
                try {
                    runnable.accept(workerContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }
}
