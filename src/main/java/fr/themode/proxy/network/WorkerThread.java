package fr.themode.proxy.network;

import java.util.concurrent.atomic.AtomicInteger;

public class WorkerThread extends Thread {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private WorkerThread(Runnable runnable) {
        super(null, runnable, "worker-" + COUNTER.getAndIncrement());
    }

    protected static WorkerThread start(Runnable runnable) {
        WorkerThread thread = new WorkerThread(() -> {
            while (true) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }
}
