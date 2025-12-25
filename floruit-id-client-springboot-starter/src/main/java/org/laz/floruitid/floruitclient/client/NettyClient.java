package org.laz.floruitid.floruitclient.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.common.exception.NettyClientException;
import org.laz.floruitid.floruitclient.model.req.ReqData;
import org.laz.floruitid.floruitclient.model.resp.RespData;
import org.laz.floruitid.floruitclient.pool.NettyChannelPool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty客户端
 */
@Slf4j
public class NettyClient {
    private final AtomicLong reqId = new AtomicLong(1);
    private final NettyChannelPool nettyChannelPool;

    public NettyClient(NettyChannelPool nettyChannelPool) {
        this.nettyChannelPool = nettyChannelPool;
    }

    public CompletableFuture<RespData> sendDataAsync(ReqData reqData) {
        ResponsePromise promise = new ResponsePromise();
        ResponsePromiseHolder.putResponsePromise(reqData.getReqId(), promise);

        Channel ch = null;
        try {
            ch = nettyChannelPool.getChannelSync();
            ch.writeAndFlush(reqData);
        } finally {
            nettyChannelPool.releaseChannel(ch);
        }

        return promise.getResponseAsync();
    }

    public RespData sendDataSync(ReqData reqData) {
        try {
            CompletableFuture<RespData> future = this.sendDataAsync(reqData);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Send Data Error", e);
            throw new NettyClientException("Send Data Error");
        }
    }

    public Long getAndIncrementReqId() {
        return reqId.getAndIncrement();
    }
}
