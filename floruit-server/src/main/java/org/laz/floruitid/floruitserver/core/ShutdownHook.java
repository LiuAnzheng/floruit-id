package org.laz.floruitid.floruitserver.core;

import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownHook extends Thread {

    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;
    private final ServerChannel serverChannel;

    public ShutdownHook(NioEventLoopGroup bossGroup,
                        NioEventLoopGroup workerGroup,
                        ServerChannel serverChannel) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverChannel = serverChannel;
    }

    @Override
    public void run() {
        try {
            serverChannel.close().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Server Shutdown Gracefully, Bye~Bye~");
        } catch (InterruptedException e) {
            log.error("Server Shutdown Fail", e);
        }
    }
}
