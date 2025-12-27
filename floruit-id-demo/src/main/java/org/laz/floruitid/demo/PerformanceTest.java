package org.laz.floruitid.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.service.IdService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class PerformanceTest {

    @Resource
    private IdService idService;

    /**
     * 模拟100个并发业务线程, 每个业务线程生成100000个ID
     */
    @PostConstruct
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        CountDownLatch ans = new CountDownLatch(1000);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int j = 0; j < 100000; j++) {
                    idService.getIdBySnowflakeMode();
                }
                ans.countDown();
            }).start();
            countDownLatch.countDown();
        }
        long start = System.currentTimeMillis();
        ans.await();
        long end = System.currentTimeMillis();
        log.info("耗时: {} ms", end - start); // 72s
    }

}
