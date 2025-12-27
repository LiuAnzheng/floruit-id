package org.laz.floruitid.floruitserver.registrycenter.redisimpl;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;

/**
 * 维护Redis连接
 */
@Slf4j
public class RedisConnectionHolder {

    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, String> connection;
    private static final ServerConfigHolder config = ServerConfigFactory.getConfig();

    private RedisConnectionHolder() {

    }

    static {
        try {
            RedisURI redisUri = RedisURI.builder()
                    .withHost(config.getRedisHost())
                    .withPort(config.getRedisPort())
                    .withAuthentication(config.getRedisUsername(), config.getRedisPassword())
                    .build();
            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
        } catch (Exception e) {
            log.warn("Redis Center Connect Error, Application will use local cache", e);
            redisClient = null;
            connection = null;
        }
    }

    /**
     * 若Redis连接失败, 则返回null, 触发降级策略
     */
    public static StatefulRedisConnection<String, String> getRedisConnection() {
        return connection;
    }

    public static void shutdownRedisGracefully() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        log.info("Redis Center Connection Shutdown");
    }

}
