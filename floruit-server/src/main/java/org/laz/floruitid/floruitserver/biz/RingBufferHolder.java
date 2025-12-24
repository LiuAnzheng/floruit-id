package org.laz.floruitid.floruitserver.biz;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import org.laz.floruitid.floruitserver.biz.exceptionhandler.DefaultExceptionHandler;
import org.laz.floruitid.floruitserver.biz.workerhandler.SegmentWorkerHandler;
import org.laz.floruitid.floruitserver.biz.workerhandler.SnowflakeWorkerHandler;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.model.event.AbstractEvent;
import org.laz.floruitid.floruitserver.model.event.DefaultEvent;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RingBuffer工具类, 维护RingBuffer和消费者
 */
public class RingBufferHolder {
    private static final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private static final RingBuffer<AbstractEvent> snowflakeRingBuffer;
    private static final RingBuffer<AbstractEvent> segmentRingBuffer;
    private static final ThreadPoolExecutor customerPool;

    static {
        // 初始化消费者线程池
        customerPool = new ThreadPoolExecutor(
                config.getCustomerPoolSize(),
                config.getCustomerPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        // 初始化RingBuffer
        if (config.getOpenSnowFlakeMode()) {
            snowflakeRingBuffer = RingBuffer.create(
                    ProducerType.MULTI,
                    DefaultEvent::new,
                    config.getRingBufferSize(),
                    new BusySpinWaitStrategy()
            );
        } else {
            snowflakeRingBuffer = null;
        }
        if (config.getOpenSegmentMode()) {
            segmentRingBuffer = RingBuffer.create(
                    ProducerType.MULTI,
                    DefaultEvent::new,
                    config.getRingBufferSize(),
                    new SleepingWaitStrategy()
            );
        } else {
            segmentRingBuffer = null;
        }
        // 初始化消费者
        if (snowflakeRingBuffer != null) {
            SequenceBarrier sb = snowflakeRingBuffer.newBarrier();
            WorkerPool<AbstractEvent> pool = new WorkerPool<>(snowflakeRingBuffer, sb, new DefaultExceptionHandler(), new SnowflakeWorkerHandler());
            snowflakeRingBuffer.addGatingSequences(pool.getWorkerSequences());
            pool.start(customerPool);
        }
        if (segmentRingBuffer != null) {
            SequenceBarrier sb = segmentRingBuffer.newBarrier();
            WorkerPool<AbstractEvent> pool = new WorkerPool<>(segmentRingBuffer, sb, new DefaultExceptionHandler(), new SegmentWorkerHandler());
            segmentRingBuffer.addGatingSequences(pool.getWorkerSequences());
            pool.start(customerPool);
        }
    }

    public static RingBuffer<AbstractEvent> getSnowflakeRingBuffer() {
        return snowflakeRingBuffer;
    }

    public static RingBuffer<AbstractEvent> getSegmentRingBuffer() {
        return segmentRingBuffer;
    }

    public static void shutdownRingBuffer() {
        if (customerPool != null) {
            customerPool.shutdown();
        }
    }
}
