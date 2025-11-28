package org.laz.floruitid.floruitserver.config;

import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.common.LocationConstant;
import org.laz.floruitid.floruitserver.exception.InitException;
import tools.jackson.dataformat.javaprop.JavaPropsMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * 解析配置文件, 映射到Java对象
 */
@Slf4j
public class ServerConfigFactory {

    private static final ServerConfigHolder configHolder;

    static {
        JavaPropsMapper mapper = new JavaPropsMapper();
        InputStream is = null;
        try {
            // 解析配置文件至内存
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(LocationConstant.CONFIG_FILE_LOCATION);
            configHolder = mapper.readValue(is, ServerConfigHolder.class);
        } catch (Exception e) {
            log.error("Read Config File Fail", e);
            throw new InitException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Read Config File Fail", e);
                }
            }
        }
    }

    public static ServerConfigHolder getConfig() {
        return configHolder;
    }
}
