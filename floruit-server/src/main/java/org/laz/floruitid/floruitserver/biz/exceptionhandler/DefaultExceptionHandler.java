package org.laz.floruitid.floruitserver.biz.exceptionhandler;

import com.lmax.disruptor.ExceptionHandler;
import org.laz.floruitid.floruitserver.model.event.AbstractEvent;

/**
 * Disruptor WorkerPool 异常处理器
 */
public class DefaultExceptionHandler implements ExceptionHandler<AbstractEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, AbstractEvent event) {

    }

    @Override
    public void handleOnStartException(Throwable ex) {

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {

    }
}
