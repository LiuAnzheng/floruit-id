package org.laz.floruitid.floruitclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 客户端配置
 */
@ConfigurationProperties(prefix = "floruit")
@Data
public class FloruitClientConfig {
    private String remoteAddr;
    private Integer remotePort;
    private Integer maxConnections;
    private Integer minConnections;
    private Integer workerThreads;
}
