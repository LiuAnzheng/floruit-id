package org.laz.floruitid.floruitserver.biz;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.laz.floruitid.floruitserver.biz.workerhandler.SegmentEventHandler;
import org.laz.floruitid.floruitserver.biz.workerhandler.SnowflakeEventHandler;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.model.event.AbstractEvent;
import org.laz.floruitid.floruitserver.model.event.DefaultEvent;

import java.util.concurrent.ThreadFactory;

/**
 * RingBuffer工具类, 维护RingBuffer和消费者
 */
public class DisruptorHolder {
    private static class SnowflakeThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.setName("Snowflake-Disruptor-Thread");
            return thread;
        }
    }

    private static class SegmentThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.setName("Segment-Disruptor-Thread");
            return thread;
        }
    }

    private static final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private static final Disruptor<AbstractEvent> snowflakeDisruptor;
    private static final Disruptor<AbstractEvent> segmentDisruptor;

    static {
        // 初始化Disruptor
        if (config.getOpenSnowFlakeMode()) {
            snowflakeDisruptor = new Disruptor<>(
                    DefaultEvent::new,
                    config.getRingBufferSize(),
                    new SnowflakeThreadFactory(),
                    ProducerType.MULTI,
                    new BusySpinWaitStrategy()
            );
        } else {
            snowflakeDisruptor = null;
        }
        if (config.getOpenSegmentMode()) {
            segmentDisruptor = new Disruptor<>(
                    DefaultEvent::new,
                    config.getRingBufferSize(),
                    new SegmentThreadFactory(),
                    ProducerType.MULTI,
                    new BusySpinWaitStrategy()
            );
        } else {
            segmentDisruptor = null;
        }
        // 初始化消费者
        if (snowflakeDisruptor != null) {
            snowflakeDisruptor.handleEventsWith(new SnowflakeEventHandler());
            snowflakeDisruptor.start();
        }
        if (segmentDisruptor != null) {
            segmentDisruptor.handleEventsWith(new SegmentEventHandler());
            segmentDisruptor.start();
        }
    }

    public static Disruptor<AbstractEvent> getSnowflakeDisruptor() {
        return snowflakeDisruptor;
    }

    public static Disruptor<AbstractEvent> getSegmentDisruptor() {
        return segmentDisruptor;
    }

    public static void shutdownDisruptor() {
        if (snowflakeDisruptor != null) {
            snowflakeDisruptor.shutdown();
        }
        if (segmentDisruptor != null) {
            segmentDisruptor.shutdown();
        }
    }
}
