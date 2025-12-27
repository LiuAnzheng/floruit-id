package org.laz.floruitid.floruitclient.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.common.exception.NettyChannelPoolException;
import org.laz.floruitid.floruitclient.config.FloruitClientConfig;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Netty连接池
 */
@Slf4j
public class NettyChannelPool {

    private final FloruitClientConfig config;
    private final NioEventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private final FixedChannelPool fixedChannelPool;

    public NettyChannelPool(FloruitClientConfig config) {
        this.config = config;
        this.workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout().intValue())
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 1024, 1024 * 1024 * 2))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                .remoteAddress(config.getRemoteAddr(), config.getRemotePort());

        this.fixedChannelPool = new FixedChannelPool(
                bootstrap,
                new NettyChannelPoolHandler(config),
                ChannelHealthChecker.ACTIVE,
                FixedChannelPool.AcquireTimeoutAction.NEW,
                config.getAcquireTimeout(),
                config.getMaxConnections(),
                Integer.MAX_VALUE,
                true,
                false
        );
    }

    public void shutdownGracefully() {
        try {
            if (fixedChannelPool != null) {
                fixedChannelPool.close();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(2, 15, TimeUnit.SECONDS).sync();
            }
        } catch (InterruptedException e) {
            log.error("Netty Shutdown Error", e);
        }
    }

    public Channel getChannelSync() {
        try {
            Future<Channel> future = this.getChannelAsync();
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Get Channel Error", e);
            throw new NettyChannelPoolException("Get Channel Error");
        }
    }

    public Future<Channel> getChannelAsync() {
        return fixedChannelPool.acquire();
    }

    public void releaseChannel(Channel channel) {
        if (channel != null) {
            fixedChannelPool.release(channel);
        }
    }
}
