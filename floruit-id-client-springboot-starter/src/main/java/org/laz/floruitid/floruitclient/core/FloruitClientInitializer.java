package org.laz.floruitid.floruitclient.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.config.FloruitClientConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 客户端初始化
 */
@RequiredArgsConstructor
@Slf4j
public class FloruitClientInitializer implements ApplicationRunner {

    private final FloruitClientConfig config;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Netty Client Connection Pool Innit Successfully, Remote Address {}, Remote Port {}",
                config.getRemoteAddr(), config.getRemotePort());
    }
}
