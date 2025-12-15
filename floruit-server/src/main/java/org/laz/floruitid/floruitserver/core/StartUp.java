package org.laz.floruitid.floruitserver.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.handler.PipelineInitializer;
import org.laz.floruitid.floruitserver.registrycenter.redisimpl.RedisRegistryCenter;

/**
 * 主启动类, 启动Netty服务器
 */
@Slf4j
public class StartUp {

    private static final StartUp nettyServerInstance = new StartUp();

    public static StartUp getNettyServerInstance() {
        return nettyServerInstance;
    }

    public static void main(String[] args) {
        // 初始化注册中心
        RedisRegistryCenter.getInstance().initRegistryCenter();
        // 启动服务器
        nettyServerInstance.startNettyServer();
    }

    private final ServerConfigHolder config;
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;
    private final ChannelInitializer<NioSocketChannel> channelInitializer;
    private final ShutdownHook shutdownHook;
    private ServerChannel serverChannel;

    private StartUp() {
        config = ServerConfigFactory.getConfig();

        bossGroup = new NioEventLoopGroup(config.getBossNum());
        workerGroup = new NioEventLoopGroup(config.getWorkerNum());

        channelInitializer = new PipelineInitializer();

        shutdownHook = new ShutdownHook();
    }

    private void startNettyServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65535)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator())
                .childHandler(channelInitializer);

        ChannelFuture cf = bootstrap.bind(config.getAddr(), config.getPort());
        serverChannel = (ServerChannel) cf.channel();
        log.info("Server Start Up Successfully: addr {} prot {}", config.getAddr(), config.getPort());

        try {
            // 注册Java进程关闭钩子
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Channel Close Sync InterruptedException", e);
        }
    }

    public void shutdownNettyGracefully() {
        if (serverChannel != null) {
            try {
                serverChannel.close().sync();
            } catch (InterruptedException e) {
                log.error("Server Channel Shutdown Error", e);
            }
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty Server Shutdown");
    }

}
