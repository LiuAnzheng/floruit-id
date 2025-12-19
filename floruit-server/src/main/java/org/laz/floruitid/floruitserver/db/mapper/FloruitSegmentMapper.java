package org.laz.floruitid.floruitserver.db.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.laz.floruitid.floruitserver.db.entity.FloruitSegmentDO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * 号段模式数据库实体映射
 */
public class FloruitSegmentMapper implements RowMapper<FloruitSegmentDO> {
    @Override
    public FloruitSegmentDO map(ResultSet rs, StatementContext ctx) throws SQLException {
        return FloruitSegmentDO.builder()
                .key(rs.getString("key"))
                .maxId(rs.getLong("max_id"))
                .step(rs.getLong("step"))
                .createTime(rs.getTimestamp("create_time",
                                Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")))
                        .toLocalDateTime())
                .updateTime(rs.getTimestamp("update_time",
                                Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")))
                        .toLocalDateTime())
                .delFlag(rs.getInt("del_flag"))
                .build();
    }
}
