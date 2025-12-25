package org.laz.floruitid.floruitclient.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.model.resp.RespData;

/**
 * 入站处理器
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RespData> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RespData msg) throws Exception {
        ResponsePromiseHolder.setResponsePromise(msg);
    }
}
