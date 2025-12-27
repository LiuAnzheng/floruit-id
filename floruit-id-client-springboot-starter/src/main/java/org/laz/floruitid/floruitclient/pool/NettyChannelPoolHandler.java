package org.laz.floruitid.floruitclient.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.client.NettyClientHandler;
import org.laz.floruitid.floruitclient.config.FloruitClientConfig;
import org.laz.floruitid.floruitclient.model.resp.RespData;

import java.util.concurrent.TimeUnit;

/**
 * Netty连接池监控
 */
@Slf4j
public class NettyChannelPoolHandler implements ChannelPoolHandler {

    private final FloruitClientConfig config;

    public NettyChannelPoolHandler(FloruitClientConfig config) {
        this.config = config;
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {

    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, config.getHeatBeatInterval(), TimeUnit.MILLISECONDS));
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(RespData.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
