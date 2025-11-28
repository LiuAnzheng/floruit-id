package org.laz.floruitid.floruitserver.registrycenter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public abstract class AbstractRegistryCenter implements RegistryCenter {

    protected static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public abstract void initRegistryCenter();

    @Override
    public abstract int getWorkerId();

    @Override
    public abstract void cacheWorkerId();

    @Override
    public abstract void scheduledUploadWorkerId();

    public static void shutdownExecutor() {
        executor.shutdown();
        log.info("Scheduled Executor Shutdown");
    }
}
