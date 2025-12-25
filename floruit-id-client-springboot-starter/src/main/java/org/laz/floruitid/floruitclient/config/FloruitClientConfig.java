package org.laz.floruitid.floruitclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 客户端配置
 */
@ConfigurationProperties(prefix = "floruit")
@Data
public class FloruitClientConfig {
    private String remoteAddr = "127.0.0.1";
    private Integer remotePort = 60000;
    private Integer maxConnections = Integer.MAX_VALUE;
    private Integer workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    private Long connectionTimeout = 5000L;
    private Long acquireTimeout = 3000L;
    private Long heatBeatInterval = 3000L;
}
