package org.laz.floruitid.floruitserver.handler;

import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.biz.RingBufferHolder;
import org.laz.floruitid.floruitserver.common.enums.IdMode;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.model.event.AbstractEvent;
import org.laz.floruitid.floruitserver.model.event.DefaultEvent;
import org.laz.floruitid.floruitserver.model.proto.req.ReqData;
import org.laz.floruitid.floruitserver.model.proto.resp.RespData;

/**
 * Netty的业务逻辑Handler, 用于将请求转发给具体的RingBuffer处理
 */
@Slf4j
public class BizHandler extends SimpleChannelInboundHandler<ReqData> {

    private static final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private static final RingBuffer<AbstractEvent> snowflakeRingBuffer = RingBufferHolder.getSnowflakeRingBuffer();
    private static final RingBuffer<AbstractEvent> segmentRingBuffer = RingBufferHolder.getSegmentRingBuffer();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqData msg) throws Exception {
        String mode = msg.getMode();
        if (IdMode.SNOW_FLAKE.getMode().equals(mode) && config.getOpenSnowFlakeMode()) {
            // 雪花算法
            snowflakeRingBuffer.publishEvent((event, sequence) -> {
                DefaultEvent e = (DefaultEvent) event;
                e.setCtx(ctx);
                e.setKey(msg.getKey());
                e.setReqId(msg.getReqId());
            });
        } else if (IdMode.SEGMENT.getMode().equals(mode) && config.getOpenSegmentMode()) {
            // 号段模式
            segmentRingBuffer.publishEvent((event, sequence) -> {
                DefaultEvent e = (DefaultEvent) event;
                e.setCtx(ctx);
                e.setKey(msg.getKey());
                e.setReqId(msg.getReqId());
            });
        } else {
            RespData resp = RespData.newBuilder()
                    .setReqId(msg.getReqId())
                    .setSuccess(false)
                    .setMessage("Mode Error")
                    .build();
            ctx.writeAndFlush(resp);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        log.error("Biz Handler Error", cause);
    }
}
