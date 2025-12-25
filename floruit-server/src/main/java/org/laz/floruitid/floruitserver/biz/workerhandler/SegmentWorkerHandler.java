package org.laz.floruitid.floruitserver.biz.workerhandler;

import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.biz.idprovider.IdProvider;
import org.laz.floruitid.floruitserver.biz.idprovider.SegmentIdProvider;
import org.laz.floruitid.floruitserver.model.event.AbstractEvent;
import org.laz.floruitid.floruitserver.model.event.DefaultEvent;
import org.laz.floruitid.floruitserver.model.proto.resp.RespData;

/**
 * Disruptor消费者
 */
@Slf4j
public class SegmentWorkerHandler implements WorkHandler<AbstractEvent> {

    private final IdProvider provider = new SegmentIdProvider();

    @Override
    public void onEvent(AbstractEvent event) throws Exception {
        try {
            DefaultEvent e = (DefaultEvent) event;
            long id = provider.getId(e.getKey());
            event.getCtx().writeAndFlush(RespData.newBuilder()
                    .setReqId(e.getReqId())
                    .setSuccess(true)
                    .setMessage("Success")
                    .setContent(String.valueOf(id))
                    .build());
        } catch (Exception e) {
            log.error("Segment Provider Error: ", e);
            event.getCtx().writeAndFlush(RespData.newBuilder()
                    .setReqId(((DefaultEvent) event).getReqId())
                    .setSuccess(false)
                    .setMessage("Error")
                    .build());
        }
    }
}
