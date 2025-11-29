package org.laz.floruitid.floruitserver.modle.event;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * RingBuffer中的Event的抽象类
 */
@Data
public abstract class AbstractEvent {
    private ChannelHandlerContext ctx;
}
