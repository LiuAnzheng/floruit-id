package org.laz.floruitid.floruitserver.biz.workerhandler;

import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.biz.idprovider.IdProvider;
import org.laz.floruitid.floruitserver.biz.idprovider.SegmentIdProvider;
import org.laz.floruitid.floruitserver.modle.event.AbstractEvent;
import org.laz.floruitid.floruitserver.modle.proto.resp.RespData;

/**
 * Disruptor消费者
 */
@Slf4j
public class SegmentWorkerHandler implements WorkHandler<AbstractEvent> {

    private final IdProvider provider = new SegmentIdProvider();

    @Override
    public void onEvent(AbstractEvent event) throws Exception {
        try {
            long id = provider.getId();
            event.getCtx().writeAndFlush(RespData.newBuilder()
                    .setId(id)
                    .setSuccess(true)
                    .setMessage("Success")
                    .build());
        } catch (Exception e) {
            log.error("Segment Provider Error: ", e);
            event.getCtx().writeAndFlush(RespData.newBuilder()
                    .setId(0L)
                    .setSuccess(false)
                    .setMessage("Error")
                    .build());
        }
    }
}
