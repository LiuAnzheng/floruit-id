package org.laz.floruitid.floruitclient.autoconfigure;

import org.laz.floruitid.floruitclient.client.NettyClient;
import org.laz.floruitid.floruitclient.config.FloruitClientConfig;
import org.laz.floruitid.floruitclient.pool.NettyChannelPool;
import org.laz.floruitid.floruitclient.pool.NettyChannelPoolLifecycle;
import org.laz.floruitid.floruitclient.service.IdService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * 自动装配Bean
 */
@AutoConfiguration
@EnableConfigurationProperties(FloruitClientConfig.class)
@ConditionalOnClass(IdService.class)
@ConditionalOnProperty(prefix = "floruit")
public class FloruitClientAutoConfiguration {

    @Bean("nettyChannelPool")
    @Order(10)
    public NettyChannelPool nettyChannelPool(FloruitClientConfig config) {
        return new NettyChannelPool(config);
    }

    @Bean("nettyClient")
    @Order(20)
    public NettyClient nettyClient(NettyChannelPool nettyChannelPool) {
        return new NettyClient(nettyChannelPool);
    }

    @Bean("nettyChannelPoolLifecycle")
    @Order(30)
    public NettyChannelPoolLifecycle nettyChannelPoolLifecycle(NettyChannelPool nettyChannelPool, FloruitClientConfig config) {
        return new NettyChannelPoolLifecycle(nettyChannelPool, config);
    }

    @Bean("idService")
    @ConditionalOnMissingBean
    @Order(40)
    public IdService idService(NettyClient nettyClient) {
        return new IdService(nettyClient);
    }
}
