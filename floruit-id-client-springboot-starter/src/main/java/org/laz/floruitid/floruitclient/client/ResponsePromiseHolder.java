package org.laz.floruitid.floruitclient.client;

import org.laz.floruitid.floruitclient.model.resp.RespData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResponsePromise管理
 */
public final class ResponsePromiseHolder {
    private static final Map<Long, ResponsePromise> responsePromiseMap = new ConcurrentHashMap<>(1 << 10);

    static void setResponsePromise(RespData respData) {
        ResponsePromise promise = responsePromiseMap.get(respData.getReqId());
        if (promise != null) {
            promise.setResponse(respData);
            responsePromiseMap.remove(respData.getReqId());
        }
    }

    static void putResponsePromise(Long reqId, ResponsePromise promise) {
        responsePromiseMap.put(reqId, promise);
    }
}
