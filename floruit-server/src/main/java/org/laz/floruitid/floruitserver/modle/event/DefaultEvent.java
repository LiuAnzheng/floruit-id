package org.laz.floruitid.floruitserver.modle.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抽象Event的空实现
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class DefaultEvent extends AbstractEvent {
    /**
     * 业务标识, 仅对号段模式生效
     */
    private String key;
}
