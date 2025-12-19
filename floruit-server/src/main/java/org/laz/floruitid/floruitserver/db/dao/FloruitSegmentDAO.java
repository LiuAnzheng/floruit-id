package org.laz.floruitid.floruitserver.db.dao;

import org.jdbi.v3.core.Jdbi;
import org.laz.floruitid.floruitserver.db.connector.JdbiUtil;
import org.laz.floruitid.floruitserver.db.entity.FloruitSegmentDO;

/**
 * 号段模式数据库实体DAO
 */
public class FloruitSegmentDAO {

    private static final FloruitSegmentDAO instance = new FloruitSegmentDAO();

    private FloruitSegmentDAO() {
    }

    public static FloruitSegmentDAO getInstance() {
        return instance;
    }

    private final Jdbi jdbi = JdbiUtil.getJdbi();

    public void insert(FloruitSegmentDO floruitSegmentDO) {
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into floruit_segment(key, max_id, step, create_time, update_time, del_flag) values(:key, :maxId, :step, :createTime, :updateTime, :delFlag)")
                        .bindBean(floruitSegmentDO)
                        .execute());
    }

    public void update(FloruitSegmentDO floruitSegmentDO) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update floruit_segment set max_id = :maxId, step = :step, update_time = :updateTime, del_flag = :delFlag where key = :key")
                        .bindBean(floruitSegmentDO)
                        .execute());
    }

    public FloruitSegmentDO select(String key) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from floruit_segment where key = :key")
                        .bind("key", key)
                        .mapToBean(FloruitSegmentDO.class)
                        .one());
    }

    public void delete(String key) {
        jdbi.withHandle(handle ->
                handle.createUpdate("delete from floruit_segment where key = :key")
                        .bind("key", key)
                        .execute());
    }
}
