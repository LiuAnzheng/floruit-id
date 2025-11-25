package org.laz.floruitid.floruitserver.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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

}
