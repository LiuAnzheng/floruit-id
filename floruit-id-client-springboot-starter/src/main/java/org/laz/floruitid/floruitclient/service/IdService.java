package org.laz.floruitid.floruitclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitclient.client.NettyClient;
import org.laz.floruitid.floruitclient.common.enums.IdMode;
import org.laz.floruitid.floruitclient.model.req.ReqData;
import org.laz.floruitid.floruitclient.model.resp.RespData;

/**
 * Id服务, 用户入口
 */
@RequiredArgsConstructor
@Slf4j
public class IdService {

    private final NettyClient nettyClient;

    /**
     * 获取ID (号段模式)
     *
     * @param key 业务key
     * @return ID
     */
    public String getIdBySegmentMod(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key is empty");
        }
        ReqData reqData = ReqData.newBuilder()
                .setMode(IdMode.SEGMENT.getMode())
                .setReqId(nettyClient.getAndIncrementReqId())
                .setKey(key)
                .build();
        RespData respData = nettyClient.sendDataSync(reqData);
        return respData.getContent();
    }

    /**
     * 获取ID (雪花算法模式)
     *
     * @return ID
     */
    public long getIdBySnowflakeMode() {
        ReqData reqData = ReqData.newBuilder()
                .setMode(IdMode.SNOW_FLAKE.getMode())
                .setReqId(nettyClient.getAndIncrementReqId())
                .build();
        RespData respData = nettyClient.sendDataSync(reqData);
        return Long.parseLong(respData.getContent());
    }

}
