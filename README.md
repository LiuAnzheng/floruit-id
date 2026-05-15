# Floruit-Id

<p align="center">
  <strong>新一代高性能分布式 ID 生成器</strong>
</p>

<p align="center">
  Java 21 &nbsp;|&nbsp; Netty 4.2 &nbsp;|&nbsp; Protobuf &nbsp;|&nbsp; LMAX Disruptor &nbsp;|&nbsp; Spring Boot 3
</p>

---

## 概述

Floruit-Id 是一个高吞吐、低延迟的分布式 ID 生成服务，提供 **雪花算法** 和 **号段模式** 两种 ID 生成策略，通过 Netty TCP + Protobuf 通信，基于 LMAX Disruptor 无锁队列实现毫秒级响应，同时提供 Spring Boot Starter 让业务接入零门槛。

### 特性

- **双模式** — 雪花算法模式满足高性能场景，号段模式支持业务定制
- **高性能** — 基于 Disruptor RingBuffer 无锁并发，单机轻松突破千万 QPS
- **时钟回拨保护** — 3ms 内自动忙等恢复，超限立即报错
- **Worker ID 自动注册** — Redis 注册中心 + 本地文件缓存双重保障
- **开箱即用** — 提供 Spring Boot Starter，注解驱动，一行配置即可接入
- **优雅启停** — Netty → Disruptor → Redis 逐级有序关闭

---

## 架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Spring Boot)                     │
│  IdService.getIdBySnowflakeMode()  →  NettyClient  →  TCP       │
└─────────────────────────────────────────────────────────────────┘
                                │
                    Protobuf (Varint32 Framing)
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Netty Pipeline                            │
│  FrameDecoder → ProtobufDecoder → BizHandler → FrameEncoder     │
└─────────────────────────────────────────────────────────────────┘
                                │
                  ┌─────────────┴─────────────┐
                  ▼                           ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│  Disruptor (Snowflake)   │  │  Disruptor (Segment)     │
│  BusySpinWaitStrategy    │  │  BusySpinWaitStrategy    │
└──────────────────────────┘  └──────────────────────────┘
                  │                           │
                  ▼                           ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│  SnowflakeIdProvider     │  │  SegmentIdProvider       │
│  · 时间戳 41bit          │  │  · MySQL 号段分配        │
│  · Worker ID 10bit       │  │  · 双 Buffer 预加载      │
│  · 序列号 12bit          │  │                          │
└──────────────────────────┘  └──────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Registry Center                             │
│  Redis (Worker ID)  +  Local File Cache (Fallback)              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 模块结构

```
floruit-id/
├── floruit-server/                           独立 Netty 服务端
├── floruit-id-client-springboot-starter/     Spring Boot 自动配置客户端
└── floruit-id-demo/                          压力测试 Demo
```

| 模块 | 说明 |
|---|---|
| `floruit-server` | 纯 Netty TCP 服务，不依赖 Spring，独立部署 |
| `floruit-id-client-springboot-starter` | Spring Boot 3 自动装配，注入 `IdService` 即可使用 |
| `floruit-id-demo` | 客户端接入示例 & 并发压测 |

---

## 快速开始

### 环境要求

- **JDK** 21+
- **Redis** (Worker ID 注册中心)
- **MySQL** 8.0+ (仅号段模式需要)

### 1. 启动服务端

**克隆 & 打包：**

```bash
git clone <your-repo-url>
cd floruit-id
mvn clean package -DskipTests
```

**配置文件** `config/floruit_server.properties`：

```properties
network.listen.addr=0.0.0.0
network.listen.port=60000

biz.open.snowflake.mode=true

center.redis.host=127.0.0.1
center.redis.port=6379
center.redis.password=

workerId.local.cache.path=/usr/local/floruit-id/workerId.txt
ringBuffer.size=65536
```

**启动：**

```bash
java -cp floruit-server.jar:lib/* org.laz.floruitid.floruitserver.core.StartUp
```

### 2. 客户端接入

**引入依赖：**

```xml
<dependency>
    <groupId>org.laz</groupId>
    <artifactId>floruit-id-client-springboot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

**配置 `application.yml`：**

```yaml
floruit:
  remote-addr: 127.0.0.1
  remote-port: 60000
```

**注入使用：**

```java
@Resource
private IdService idService;

// 雪花算法 - 返回 Long
long id = idService.getIdBySnowflakeMode();

// 号段模式 - 返回 String
String id = idService.getIdBySegmentMod("order");
```

---

## 配置参考

### 服务端配置

| 属性 | 默认值 | 说明 |
|---|---|---|
| `network.listen.addr` | `127.0.0.1` | 监听地址 |
| `network.listen.port` | `60000` | 监听端口 |
| `network.boss-group-num` | `1` | Netty Boss 线程数 |
| `network.worker-group-num` | `CPU × 4` | Netty Worker 线程数 |
| `biz.open.snowflake.mode` | `false` | 开启雪花算法模式 |
| `biz.open.segment.mode` | `false` | 开启号段模式 |
| `center.redis.host` | `127.0.0.1` | Redis 地址 |
| `center.redis.port` | `6379` | Redis 端口 |
| `center.redis.password` | — | Redis 密码 |
| `workerId.local.cache.path` | `/usr/local/floruit-id/workerId.txt` | Worker ID 本地缓存 |
| `workerId.default` | `0` | 默认 Worker ID |
| `ringBuffer.size` | `65536` | Disruptor RingBuffer 大小 (2 的幂) |
| `snowflake.epoch` | `1765781974706` | 雪花算法起始时间戳 (ms) |
| `segment.mysql.url` | — | MySQL JDBC 连接 (号段模式) |
| `segment.max-qps` | `1000` | 号段模式 QPS 上限 |

### 客户端配置

| 属性 | 默认值 | 说明 |
|---|---|---|
| `floruit.remote-addr` | `127.0.0.1` | 服务端地址 |
| `floruit.remote-port` | `60000` | 服务端端口 |
| `floruit.max-connections` | `Integer.MAX_VALUE` | 连接池大小 |
| `floruit.worker-threads` | `CPU × 2` | Netty Worker 线程数 |
| `floruit.connection-timeout` | `5000` | 连接超时 (ms) |
| `floruit.acquire-timeout` | `3000` | 通道获取超时 (ms) |
| `floruit.heat-beat-interval` | `3000` | 心跳检测间隔 (ms) |
| `floruit.enable` | `true` | 启用自动配置 |

---

## ID 位布局 (雪花算法)

```
┌─┬──────────────────────────────────┬──────────────────┬────────────────────┐
│0│        时间戳差 (41 bit)          │  Worker ID (10b) │   序列号 (12 bit)   │
└─┴──────────────────────────────────┴──────────────────┴────────────────────┘
   ←──────────────────────────── 64 bit ────────────────────────────────────→
```

- **1 bit** — 符号位，恒为 0
- **41 bit** — 相对 epoch 的时间戳，可用约 69 年
- **10 bit** — Worker ID，最多 1024 个节点
- **12 bit** — 毫秒内序列号，每毫秒 4096 个 ID

> 理论单机吞吐量：**409.6 万 / 秒**

---

## 通信协议

基于 Protobuf 定义，Varint32 长度前缀成帧：

```protobuf
// 请求
message ReqData {
  int64 reqId = 1;   // 请求 ID (响应对应)
  string mode = 2;   // "snowflake" | "segment"
  string key  = 3;   // 业务标识 (号段模式)
}

// 响应
message RespData {
  int64  reqId   = 1;  // 对应请求 ID
  bool   success = 2;  // 是否成功
  string message = 3;  // 消息
  string content = 4;  // 生成的 ID
}
```

---

## 压力测试

**测试环境**：Ubuntu 24.04 / 4C8G

**场景**：100 并发线程 × 100000 次 = **1000 万**雪花 ID

```java
// floruit-id-demo → PerformanceTest
CountDownLatch latch = new CountDownLatch(1000);
// 100 threads, each calling getIdBySnowflakeMode() 100,000 times
```

> 持续优化中，欢迎提交更多基准测试数据。

---

## 项目依赖

| 技术 | 版本 | 用途 |
|---|---|---|
| Java | 21 | 运行环境 |
| Netty | 4.2.4 | TCP 通信框架 |
| Protobuf | 4.33.1 | 序列化协议 |
| LMAX Disruptor | 3.4.4 | 高性能 RingBuffer |
| Lettuce | 7.1.0 | Redis 客户端 |
| JDBI | 3.51.0 | 数据库访问 |
| MySQL Connector | 8.4.0 | MySQL JDBC |
| Spring Boot | 3.5.9 | 客户端自动装配 |
| Lombok | 1.18.42 | 代码简化 |

---

## License

MIT
