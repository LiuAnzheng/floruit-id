package org.laz.floruitid.floruitclient.client;

import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.model.resp.RespData;

import java.util.concurrent.CompletableFuture;

/**
 * 响应结果Future
 */
@Slf4j
public class ResponsePromise {

    private final CompletableFuture<RespData> completableFuture = new CompletableFuture<>();

    void setResponse(RespData response) {
        completableFuture.complete(response);
    }

    CompletableFuture<RespData> getResponseAsync() {
        return completableFuture;
    }

}
