package org.laz.floruitid.floruitserver.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用于在注册中心注册WorkerId的实体类
 */
@Data
public class WorkerIdDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer workerId;

    private Long timestamp;
}
