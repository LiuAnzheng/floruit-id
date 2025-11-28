package org.laz.floruitid.floruitserver.registrycenter;

/**
 * 注册中心统一接口
 */
public interface RegistryCenter {

    /**
     * 初始化注册中心
     */
    void initRegistryCenter();

    /**
     * 得到本机的workerId
     */
    int getWorkerId();

    /**
     * 将workerId缓存至本地
     */
    void cacheWorkerId();

    /**
     * 定时上报workerId和本机时间给注册中心
     */
    void scheduledUploadWorkerId();
}
