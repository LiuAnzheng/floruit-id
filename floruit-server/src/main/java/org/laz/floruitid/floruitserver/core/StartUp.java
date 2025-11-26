package org.laz.floruitid.floruitserver.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.exception.InitException;
import org.laz.floruitid.floruitserver.handler.PipelineInitializer;

@Slf4j
public class StartUp {

    public static void main(String[] args) {
        // 启动服务器
        new StartUp().startNettyServer();
    }

    private ServerConfigHolder config;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ChannelInitializer<NioSocketChannel> channelInitializer;

    private StartUp() {
        config = ServerConfigFactory.getConfig();

        bossGroup = new NioEventLoopGroup(config.getBossNum());
        workerGroup = new NioEventLoopGroup(config.getWorkerNum());

        channelInitializer = new PipelineInitializer();
    }

    private void startNettyServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65535)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator())
                .option(ChannelOption.TCP_FASTOPEN, 65535)
                .handler(new LoggingHandler(LogLevel.WARN))
                .childHandler(channelInitializer);

        ChannelFuture cf = bootstrap.bind(config.getAddr(), config.getPort());
        log.info("Server Start Up Successfully: addr {} prot {}", config.getAddr(), config.getPort());

        try {
            // 注册Java进程关闭钩子
            ShutdownHook hook = new ShutdownHook(bossGroup, workerGroup, (ServerChannel) cf.channel());
            Runtime.getRuntime().addShutdownHook(hook);
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Channel Close Sync InterruptedException", e);
            throw new InitException(e.getMessage());
        }
    }

}
