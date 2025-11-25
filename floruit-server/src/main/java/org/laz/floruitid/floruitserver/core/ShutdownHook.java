package org.laz.floruitid.floruitserver.core;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownHook extends Thread {

    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    public ShutdownHook(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
    }

    @Override
    public void run() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("Application Shutdown Gracefully, Bye~Bye~");
    }
}
