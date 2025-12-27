package org.laz.floruitid.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.service.IdService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FloruitIdDemoApp {

    @Resource
    private IdService idService;

    public static void main(String[] args) {
        SpringApplication.run(FloruitIdDemoApp.class, args);
    }

    @PostConstruct
    public void init() {
        long id = idService.getIdBySnowflakeMode();
        log.info("Test Server Snowflake Id: {}", id);
        String pk = idService.getIdBySegmentMod("test");
        log.info("Test Server Segment Id: {}", pk);
    }
}
