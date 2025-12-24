package org.laz.floruitid.floruitclient.autoconfigure;

import org.laz.floruitid.floruitclient.config.FloruitClientConfig;
import org.laz.floruitid.floruitclient.core.FloruitClientInitializer;
import org.laz.floruitid.floruitclient.service.IdService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 注入Bean
 */
@AutoConfiguration
@EnableConfigurationProperties(FloruitClientConfig.class)
@ConditionalOnClass(IdService.class)
@ConditionalOnProperty(prefix = "floruit")
public class FloruitClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdService idService() {
        return new IdService();
    }

    @Bean
    public FloruitClientInitializer floruitClientInitializer(FloruitClientConfig config) {
        return new FloruitClientInitializer(config);
    }

}
