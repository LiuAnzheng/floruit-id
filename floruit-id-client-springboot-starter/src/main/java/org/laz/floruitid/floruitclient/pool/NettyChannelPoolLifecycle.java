package org.laz.floruitid.floruitclient.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.config.FloruitClientConfig;
import org.springframework.context.SmartLifecycle;

/**
 * Netty连接池生命周期
 */
@Slf4j
@RequiredArgsConstructor
public class NettyChannelPoolLifecycle implements SmartLifecycle {
    private final NettyChannelPool nettyChannelPool;
    private final FloruitClientConfig config;

    @Override
    public void start() {
        log.info("Netty Channel Pool Start Successfully Remote Addr {} Remote Port {}", config.getRemoteAddr(), config.getRemotePort());
    }

    @Override
    public void stop() {
        log.info("Netty Channel Pool Stopping...");
        nettyChannelPool.shutdownGracefully();
        log.info("Netty Channel Pool Stop Successfully, Bye~Bye~");
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
