package org.laz.floruitid.floruitclient.pool;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty连接池监控
 */
@Slf4j
public class NettyChannelPoolHandler implements ChannelPoolHandler {
    @Override
    public void channelReleased(Channel ch) throws Exception {

    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {

    }

    @Override
    public void channelCreated(Channel ch) throws Exception {

    }
}
