package org.laz.floruitid.floruitserver.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 映射properties中的配置
 */
@Data
public class ServerConfigHolder {

    @JsonProperty(value = "network.listen.addr")
    private String addr = "127.0.0.1";

    @JsonProperty(value = "network.listen.port")
    private Integer port = 60000;

    @JsonProperty(value = "network.boss-group-num")
    private Integer bossNum = 1;

    @JsonProperty(value = "network.worker-group-num")
    private Integer workerNum = Runtime.getRuntime().availableProcessors() * 4;

    @JsonProperty(value = "biz.open.segment.mode")
    private Boolean openSegmentMode = true;

    @JsonProperty(value = "biz.open.snowflake.mode")
    private Boolean openSnowFlakeMode = true;

    @JsonProperty(value = "center.redis.url")
    private String redisUrl = "redis://127.0.0.1:6379";

    @JsonProperty(value = "workerId.local.cache.path")
    private String workerIdCachePath = "D:/workerId.txt";

    @JsonProperty(value = "workerId.default")
    private Integer defaultWorkerId = 0;

    @JsonProperty(value = "ringBuffer.size")
    private Integer ringBufferSize = 65535;

    @JsonProperty(value = "customer.pool.size")
    private Integer customerPoolSize = 512;
}
