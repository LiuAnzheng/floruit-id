package org.laz.floruitid.floruitserver.core;

import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.biz.RingBufferHolder;
import org.laz.floruitid.floruitserver.registrycenter.AbstractRegistryCenter;
import org.laz.floruitid.floruitserver.registrycenter.redisimpl.RedisConnectionHolder;

/**
 * Java进程关闭钩子
 */
@Slf4j
public class ShutdownHook extends Thread {

    @Override
    public void run() {
        // Shutdown Netty
        StartUp.getNettyServerInstance().shutdownNettyGracefully();
        // Shutdown Disruptor
        RingBufferHolder.shutdownRingBuffer();
        // Shutdown Scheduled Task
        AbstractRegistryCenter.shutdownExecutor();
        // Shutdown Redis
        RedisConnectionHolder.shutdownRedisGracefully();
        log.info("Server Shutdown Gracefully, Bye~Bye~");
    }
}
