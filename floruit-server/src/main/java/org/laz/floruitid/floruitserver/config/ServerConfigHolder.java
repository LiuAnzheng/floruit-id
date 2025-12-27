package org.laz.floruitid.floruitserver.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 映射properties中的配置
 */
@Data
public final class ServerConfigHolder {

    @JsonProperty(value = "network.listen.addr")
    private String addr = "127.0.0.1";

    @JsonProperty(value = "network.listen.port")
    private Integer port = 60000;

    @JsonProperty(value = "network.boss-group-num")
    private Integer bossNum = 1;

    @JsonProperty(value = "network.worker-group-num")
    private Integer workerNum = Runtime.getRuntime().availableProcessors() * 4;

    @JsonProperty(value = "biz.open.segment.mode")
    private Boolean openSegmentMode = false;

    @JsonProperty(value = "biz.open.snowflake.mode")
    private Boolean openSnowFlakeMode = false;

    @JsonProperty(value = "center.redis.host")
    private String redisHost = "127.0.0.1";

    @JsonProperty(value = "center.redis.port")
    private Integer redisPort = 6379;

    @JsonProperty(value = "center.redis.password")
    private String redisPassword = "";

    @JsonProperty(value = "center.redis.username")
    private String redisUsername = "";

    @JsonProperty(value = "workerId.local.cache.path")
    private String workerIdCachePath = "/usr/local/floruit-id/workerId.txt";

    @JsonProperty(value = "workerId.default")
    private Integer defaultWorkerId = 0;

    @JsonProperty(value = "ringBuffer.size")
    private Integer ringBufferSize = 65536;

    @JsonProperty(value = "snowflake.epoch")
    private Long snowflakeEpoch = 1765781974706L;

    @JsonProperty(value = "segment.mysql.url")
    private String mysqlUrl = "jdbc:mysql://127.0.0.1:3306/floruitid?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true";

    @JsonProperty(value = "segment.mysql.user")
    private String mysqlUser = "root";

    @JsonProperty(value = "segment.mysql.password")
    private String mysqlPassword = "root";

    @JsonProperty(value = "segment.max-qps")
    private Long maxQps = 1000L;
}
