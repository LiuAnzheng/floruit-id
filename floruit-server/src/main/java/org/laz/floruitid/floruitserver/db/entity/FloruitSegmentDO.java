package org.laz.floruitid.floruitserver.db.entity;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 号段模式数据库实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FloruitSegmentDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String key;

    private Long maxId;

    private Long step;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;
}
