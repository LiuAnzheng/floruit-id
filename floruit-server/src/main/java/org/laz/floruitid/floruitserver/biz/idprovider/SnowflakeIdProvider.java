package org.laz.floruitid.floruitserver.biz.idprovider;

import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.exception.ClockRollbackException;
import org.laz.floruitid.floruitserver.registrycenter.RegistryCenter;
import org.laz.floruitid.floruitserver.registrycenter.redisimpl.RedisRegistryCenter;

/**
 * 雪花模式ID生成逻辑
 */
public class SnowflakeIdProvider implements IdProvider {

    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final RegistryCenter registryCenter = RedisRegistryCenter.getInstance();
    private final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private final long epoch = config.getSnowflakeEpoch();
    private final long workerId = registryCenter.getWorkerId();
    private long sequence = 0L;
    private long lastTimeStamp = -1L;

    public SnowflakeIdProvider() {
        if (epoch >= System.currentTimeMillis()) {
            throw new ClockRollbackException("Epoch must be less than CurrentTimeMillis");
        }
    }

    @Override
    public long getId(String key) {
        long timeStamp = System.currentTimeMillis();
        // 时钟回拨校验
        if (timeStamp < lastTimeStamp) {
            long offset = lastTimeStamp - timeStamp;
            if (offset <= 3) {
                // 自旋offset的两倍
                this.busySpain(offset << 1);
                // 自旋结束后重新校验
                timeStamp = System.currentTimeMillis();
                if (timeStamp < lastTimeStamp) {
                    throw new ClockRollbackException("Clock Rollback");
                }
            } else {
                throw new ClockRollbackException("Clock Rollback");
            }
        }
        // 执行到此处, 说明 timeStamp >= lastTimeStamp
        if (timeStamp == lastTimeStamp) {
            // 同一毫秒, 需更新sequence
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 毫秒内序列已满, 需等待下一毫秒
                timeStamp = this.waitNextMillis();
            }
        }
        lastTimeStamp = timeStamp;
        return ((timeStamp - epoch) << (WORKER_ID_BITS + SEQUENCE_BITS)) | (workerId << SEQUENCE_BITS) | sequence;
    }

    /**
     * 使得当前线程忙等指定毫秒数
     *
     * @param millis 毫秒数
     */
    private void busySpain(long millis) {
        if (millis <= 0) {
            return;
        }
        long startNano = System.nanoTime();
        long targetNano = startNano + millis * 1000000L;
        while (true) {
            long nowNano = System.nanoTime();
            if (nowNano >= targetNano) {
                break;
            }
            Thread.yield();
        }
    }

    /**
     * 等待下一毫秒
     *
     * @return 下一毫秒
     */
    private long waitNextMillis() {
        long timeStamp = System.currentTimeMillis();
        while (timeStamp <= lastTimeStamp) {
            timeStamp = System.currentTimeMillis();
        }
        return timeStamp;
    }
}
