# Floruit-ID

<p align="center">
  <strong>⚡ 高性能、低延迟的分布式 ID 生成服务</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Netty-4.2.4-00A95C?style=flat-square" alt="Netty 4.2.4">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 3.5.9">
  <img src="https://img.shields.io/badge/Protobuf-4.33.1-4285F4?style=flat-square" alt="Protobuf">
  <img src="https://img.shields.io/badge/Disruptor-3.4.4-FF6B6B?style=flat-square" alt="Disruptor">
  <img src="https://img.shields.io/badge/License-Apache%202.0-D22128?style=flat-square" alt="License">
</p>

---

## 📖 简介

Floruit-ID 是一个基于 **Netty + Disruptor** 构建的高性能分布式 ID 生成系统。系统采用 C/S 架构，服务端基于 RingBuffer 事件驱动模型实现超高吞吐，客户端以 Spring Boot Starter 形式提供，开箱即用。

### 核心特性

- **双模式支持** — 雪花算法（Snowflake）与号段模式（Segment），适应不同业务场景
- **超高吞吐** — 基于 LMAX Disruptor RingBuffer + BusySpin 等待策略，单机可支撑百万级 QPS
- **时钟回拨防护** — 毫秒级回拨自旋等待 + 异常告警双重保障
- **WorkerID 自动注册** — 基于 Redis 注册中心，自动分配与定时续约
- **连接池复用** — 客户端 Netty 长连接池化，避免频繁建连开销
- **Spring Boot 集成** — 一行配置即可接入，原生 Spring Boot Starter 体验
- **优雅关闭** — 基于 ShutdownHook 的资源链式关闭，避免服务中断

---

## 🏗️ 项目结构

```
floruit-id
├── floruit-server                              # 服务端 — ID 生成核心引擎
│   └── src/main/java/org/laz/floruitid/floruitserver
│       ├── biz/
│       │   ├── DisruptorHolder.java            # RingBuffer 容器（雪花 / 号段双通道）
│       │   ├── idprovider/
│       │   │   ├── IdProvider.java             # ID 生成策略接口
│       │   │   ├── SnowflakeIdProvider.java    # 雪花算法实现
│       │   │   └── SegmentIdProvider.java      # 号段模式实现
│       │   └── workerhandler/
│       │       ├── SnowflakeEventHandler.java  # 雪花 Disruptor 消费者
│       │       └── SegmentEventHandler.java    # 号段 Disruptor 消费者
│       ├── config/                             # 配置映射（Properties → POJO）
│       ├── core/
│       │   ├── StartUp.java                    # Netty 服务器启动入口
│       │   └── ShutdownHook.java               # JVM 关闭钩子
│       ├── db/                                 # 号段模式数据库 DAO（JDBI）
│       ├── handler/
│       │   ├── PipelineInitializer.java        # Netty Pipeline 初始化（Protobuf 编解码）
│       │   └── BizHandler.java                 # 业务分发 → Disruptor RingBuffer
│       ├── model/proto/                        # Protobuf 请求 / 响应模型
│       └── registrycenter/                     # 注册中心（Redis 实现）
│           └── redisimpl/
│               ├── RedisRegistryCenter.java    # WorkerID 管理 + 时钟上报
│               └── RedisConnectionHolder.java  # Lettuce 连接管理
│
├── floruit-id-client-springboot-starter         # 客户端 Starter
│   └── src/main/java/org/laz/floruitid/floruitclient
│       ├── autoconfigure/                      # Spring Boot 自动装配
│       ├── client/
│       │   ├── NettyClient.java                # 同步 / 异步 ID 请求
│       │   ├── NettyClientHandler.java         # 响应处理器
│       │   └── ResponsePromiseHolder.java      # Promise 回调容器
│       ├── config/                             # 客户端配置属性
│       ├── pool/                               # Netty FixedChannelPool 连接池
│       └── service/
│           └── IdService.java                  # 用户入口 API
│
└── floruit-id-demo                              # 示例 & 压测模块
    └── src/main/java/org/laz/floruitid/demo
        ├── FloruitIdDemoApp.java               # Spring Boot 启动类
        └── PerformanceTest.java                # 100 并发 × 10 万 ID 压测
```

---

## ⚙️ 核心架构

```
                           ┌─────────────────────────────────┐
                           │         Floruit Server           │
                           │                                 │
   Client App              │  Netty TCP Server               │
   ┌──────────┐            │  ┌───────────────────────────┐  │
   │ IdService │──Req─────▶│  │ Pipeline (Protobuf Codec)  │  │
   └──────────┘            │  │            │                │  │
                           │  │       BizHandler             │  │
                           │  │     ╱              ╲         │  │
                           │  │  Snowflake       Segment     │  │
                           │  │  RingBuffer      RingBuffer  │  │
                           │  │  (Disruptor)    (Disruptor)  │  │
                           │  │     │               │         │  │
                           │  │  SnowflakeId    SegmentId    │  │
                           │  │  Provider       Provider     │  │
                           │  └───────────────────────────┘  │
                           │         │            │           │
                           │    Redis Ctr      MySQL DB       │
                           │   (WorkerID)    (号段持久化)     │
                           └─────────────────────────────────┘
```

### 请求链路

1. **客户端** 通过 `IdService` 发起请求 → `NettyClient` 从连接池获取 Channel → 发送 Protobuf 请求
2. **服务端** `BizHandler` 接收请求 → 根据 `mode` 字段路由到对应的 Disruptor RingBuffer
3. **EventHandler**（消费者）从 RingBuffer 取出事件 → 调用 `IdProvider.getId()` 生成 ID
4. 生成的 ID 通过同一个 Channel 写回客户端，客户端通过 `reqId` 匹配 Promise 返回结果

### Snowflake ID 结构

```
┌─┬──────────────────────────────────────────────────────────────┬──────────────┬────────────────┐
│1│                   41 bit Timestamp (ms)                       │ 10 bit WID   │ 12 bit Seq     │
└─┴──────────────────────────────────────────────────────────────┴──────────────┴────────────────┘
  符号位 (保留)                                                  Worker ID     毫秒内序列号
```

- **41 bit** 时间戳（相对 epoch），理论可用 69 年
- **10 bit** Worker ID，支持 512 个节点
- **12 bit** 序列号，单毫秒 4096 个 ID
- **时钟回拨防护**：≤ 3ms 时自旋等待，> 3ms 时抛出异常

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Maven | 3.6+ |
| Redis | 6.0+（雪花模式注册中心） |
| MySQL | 8.0+（号段模式持久化） |

### 1. 启动服务端

**配置 `floruit_server.properties`：**

```properties
# 网络配置
network.listen.addr=127.0.0.1
network.listen.port=60000

# 开启雪花模式
biz.open.snowflake.mode=true
# 开启号段模式
biz.open.segment.mode=true

# Redis 注册中心
center.redis.host=192.168.1.100
center.redis.port=6379
center.redis.password=your_password

# WorkerID 本地缓存（Redis 不可用时兜底）
workerId.local.cache.path=/usr/local/floruit-id/workerId.txt

# RingBuffer 大小（建议 2^n）
ringBuffer.size=65536

# Snowflake 纪元起点（ms 时间戳，须 < 当前时间）
snowflake.epoch=1765781974706

# 号段模式 MySQL
segment.mysql.url=jdbc:mysql://192.168.1.100:3306/floruit?...
segment.mysql.user=root
segment.mysql.password=your_password
```

**编译运行：**

```bash
# 编译
mvn clean package -pl floruit-server -am

# 启动
java -jar floruit-server/target/floruit-server-1.0.jar
```

### 2. 客户端接入

**Maven 依赖：**

```xml
<dependency>
    <groupId>org.laz</groupId>
    <artifactId>floruit-id-client-springboot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

**`application.yml` 配置：**

```yaml
floruit:
  remote-addr: 127.0.0.1       # 服务端地址
  remote-port: 60000            # 服务端端口
  max-connections: 512          # 最大连接数
  connection-timeout: 5000      # 连接超时 (ms)
  acquire-timeout: 3000         # 获取连接超时 (ms)
```

**代码调用：**

```java
@Resource
private IdService idService;

// 雪花模式 — 返回 long
long id = idService.getIdBySnowflakeMode();

// 号段模式 — 返回 string（按业务 key 隔离）
String id = idService.getIdBySegmentMod("order");
```

---

## 📊 性能

Demo 模块内置压力测试：**100 个并发线程，每线程生成 10 万个 ID（合计 1000 万级）**，在标准 PC 环境下完成耗时约 70s，折算单机 QPS ≈ **14 万+**（含网络往返开销）。

```java
// PerformanceTest.java — 100 并发 × 100,000 ID
@Component
public class PerformanceTest {
    @Resource
    private IdService idService;

    @PostConstruct
    public void test() throws InterruptedException {
        // 100 线程，每线程 100,000 次请求 ...
    }
}
```

> 实际吞吐受网络延迟、RingBuffer 大小、CPU 核心数等因素影响，可调整参数进一步优化。

---

## 🔧 配置参考

### 服务端配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `network.listen.addr` | `127.0.0.1` | 监听地址 |
| `network.listen.port` | `60000` | 监听端口 |
| `network.boss-group-num` | `1` | Boss 线程数 |
| `network.worker-group-num` | `CPU × 4` | Worker 线程数 |
| `biz.open.snowflake.mode` | `false` | 开启雪花模式 |
| `biz.open.segment.mode` | `false` | 开启号段模式 |
| `ringBuffer.size` | `65536` | Disruptor RingBuffer 大小 |
| `snowflake.epoch` | `1765781974706` | 雪花算法纪元起点 |
| `center.redis.host` | `127.0.0.1` | Redis 地址 |
| `center.redis.port` | `6379` | Redis 端口 |
| `workerId.local.cache.path` | `/usr/local/floruit-id/workerId.txt` | WorkerID 本地缓存 |
| `workerId.default` | `0` | 默认 WorkerID |
| `segment.mysql.url` | - | 号段模式数据库连接 |
| `segment.max-qps` | `1000` | 号段模式最大 QPS |

### 客户端配置（Spring Boot）

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `floruit.remote-addr` | `127.0.0.1` | 远程服务端地址 |
| `floruit.remote-port` | `60000` | 远程服务端端口 |
| `floruit.max-connections` | `2147483647` | 连接池最大连接数 |
| `floruit.worker-threads` | `CPU × 2` | I/O 工作线程数 |
| `floruit.connection-timeout` | `5000` | 连接超时 (ms) |
| `floruit.acquire-timeout` | `3000` | 获取连接超时 (ms) |
| `floruit.heat-beat-interval` | `3000` | 心跳间隔 (ms) |

---

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| **Netty 4.2** | 高性能 NIO 网络通信框架 |
| **LMAX Disruptor** | 无锁 RingBuffer 事件处理，极限吞吐 |
| **Protobuf 4** | 高效二进制序列化协议 |
| **Spring Boot 3.5** | 客户端自动装配 & 依赖注入 |
| **Lettuce 7** | 异步 Redis 客户端 |
| **JDBI 3** | 轻量级数据库访问层 |
| **Jackson 3** | JSON 序列化（WorkerID DTO） |
| **Lombok** | 消除样板代码 |

---

## 📝 License

Apache License 2.0

---

## 🤝 致谢

项目命名 `Floruit` 源自拉丁语，意为"繁荣 / 蓬勃发展"，寓意系统在高并发场景下稳定可靠、持续演进。
