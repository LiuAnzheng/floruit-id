package org.laz.floruitid.floruitserver.registrycenter.redisimpl;

import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.laz.floruitid.floruitserver.common.RedisCenterConstant;
import org.laz.floruitid.floruitserver.config.ServerConfigFactory;
import org.laz.floruitid.floruitserver.config.ServerConfigHolder;
import org.laz.floruitid.floruitserver.exception.ClockRollbackException;
import org.laz.floruitid.floruitserver.exception.InitException;
import org.laz.floruitid.floruitserver.modle.dto.WorkerIdDTO;
import org.laz.floruitid.floruitserver.registrycenter.AbstractRegistryCenter;
import org.laz.floruitid.floruitserver.registrycenter.RegistryCenter;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心的Redis实现
 */
@Slf4j
public class RedisRegistryCenter extends AbstractRegistryCenter {

    private static final RegistryCenter INSTANCE = new RedisRegistryCenter();

    public static RegistryCenter getInstance() {
        return INSTANCE;
    }

    private static final int MAX_WORKER_ID = 512;
    private final ServerConfigHolder config = ServerConfigFactory.getConfig();
    private final StatefulRedisConnection<String, String> connection = RedisConnectionHolder.getRedisConnection();
    private Integer workerId = config.getDefaultWorkerId();
    private Long lastSubmitTime = -1L;

    @Override
    public void initRegistryCenter() {
        if (connection == null) {
            // Redis宕机, 从本地缓存读取workerId
            File file = new File(config.getWorkerIdCachePath());
            if (!file.exists()) {
                // 本地缓存不存在
                throw new InitException("WorkerId Local Cache Not Exists");
            }
            // 读取文件
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                workerId = Integer.parseInt(br.readLine());
                if (workerId >= MAX_WORKER_ID) {
                    throw new InitException(("WorkerId Exceed Max Value: " + MAX_WORKER_ID));
                }
            } catch (IOException e) {
                throw new InitException("WorkerId Local Cache File Format Error");
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("BufferedReader Close Fail");
                    }
                }
            }
        } else {
            // 从Redis中读取workerId
            String key = String.format(RedisCenterConstant.WORKER_ID_KEY, config.getAddr(), config.getPort());
            // 存在自己的缓存
            if (connection.sync().exists(key) != 0L) {
                String json = connection.sync().get(key);
                WorkerIdDTO workerIdDTO = new ObjectMapper().readValue(json, WorkerIdDTO.class);
                // 是否发生时钟回拨
                if (workerIdDTO.getTimestamp() > System.currentTimeMillis()) {
                    throw new ClockRollbackException();
                }
                workerId = workerIdDTO.getWorkerId();
                if (workerId >= MAX_WORKER_ID) {
                    throw new InitException(("WorkerId Exceed Max Value: " + MAX_WORKER_ID));
                }
            } else {
                // 初始化Redis
                if (workerId >= MAX_WORKER_ID) {
                    throw new InitException(("WorkerId Exceed Max Value: " + MAX_WORKER_ID));
                }
                WorkerIdDTO dto = new WorkerIdDTO();
                dto.setTimestamp(System.currentTimeMillis());
                dto.setWorkerId(workerId);
                String json = new ObjectMapper().writeValueAsString(dto);
                connection.sync().set(key, json);
            }
        }
        // 设置定时任务, 定时上报本机的时间
        scheduledUploadWorkerId();
        // 更新本地缓存
        cacheWorkerId();
    }

    @Override
    public long getWorkerId() {
        return workerId;
    }

    @Override
    public void cacheWorkerId() {
        File file = new File(config.getWorkerIdCachePath());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
            pw.write(String.valueOf(workerId));
            pw.flush();
        } catch (FileNotFoundException e) {
            throw new InitException("Cache WorkerId Error");
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    @Override
    public void scheduledUploadWorkerId() {
        if (connection != null) {
            executor.scheduleAtFixedRate(() -> {
                try {
                    if (lastSubmitTime > System.currentTimeMillis()) {
                        throw new ClockRollbackException();
                    }
                    WorkerIdDTO workerIdDTO = new WorkerIdDTO();
                    workerIdDTO.setWorkerId(workerId);
                    workerIdDTO.setTimestamp(System.currentTimeMillis());
                    String json = new ObjectMapper().writeValueAsString(workerIdDTO);
                    String key = String.format(RedisCenterConstant.WORKER_ID_KEY, config.getAddr(), config.getPort());
                    connection.sync().set(key, json);
                    lastSubmitTime = System.currentTimeMillis();
                } catch (Exception e) {
                    log.error("Scheduled Upload WorkerId Error", e);
                }
            }, 0L, 1000L, TimeUnit.MILLISECONDS);
        }
    }
}
