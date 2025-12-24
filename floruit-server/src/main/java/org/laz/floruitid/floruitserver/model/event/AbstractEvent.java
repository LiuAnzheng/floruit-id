package org.laz.floruitid.floruitserver.model.event;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * RingBuffer中的Event的抽象类
 */
@Data
public abstract class AbstractEvent {
    protected ChannelHandlerContext ctx;
}
