package fr.themode.proxy.utils;

import fr.themode.proxy.network.Server;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class TaskUtils {

    public static void startSelector(Runnable runnable) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, Server.SELECTOR_TIMER, TimeUnit.MILLISECONDS);
    }
}
