package org.laz.floruitid.floruitserver.biz.idprovider;

/**
 * ID生成逻辑(算法)抽象
 */
public interface IdProvider {
    /**
     * 获取分布式ID
     *
     * @param key 业务标识, 仅对号段模式生效
     */
    long getId(String key);
}
