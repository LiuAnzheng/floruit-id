package org.laz.floruitid.floruitserver.db.connector;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.db.mapper.FloruitSegmentMapper;
import org.laz.floruitid.floruitserver.exception.InitException;

/**
 * JDBI连接工具类
 */
@Slf4j
public final class JdbiUtil {

    private static final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private static Jdbi jdbi;

    static {
        // 初始化JDBI
        try {
            jdbi = Jdbi.create(config.getMysqlUrl(), config.getMysqlUser(), config.getMysqlPassword());
        } catch (Exception e) {
            if (Boolean.TRUE.equals(config.getOpenSegmentMode())) {
                log.error("JDBI Init Fail", e);
                throw new InitException("JDBI Init Fail");
            } else {
                log.info("JDBI Init Fail, But Segment Mode Not Open");
            }
        }
        // 配置实体类映射
        if (jdbi != null) {
            jdbi.registerRowMapper(new FloruitSegmentMapper());
        }
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }

}
