# VideoShare — 视频分享平台

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 + MyBatis + Maven 多模块 |
| 微服务 | Spring Cloud Gateway + Nacos（注册中心 + 配置中心，8848）+ OpenFeign（预留） |
| 数据库 | MySQL 8.0 / Redis（缓存+令牌+推荐）/ Elasticsearch |
| 视频处理 | FFmpeg HLS 转码（hwaccel 自动适配）+ hls.js |
| 前端 | Vue 3 + Vite 4 + Element Plus + Pinia + Chart.js |
| 测试 | Playwright E2E |

## 项目结构

```
videoshare-backend/            # Maven 多模块
├── common/                    # 共享：实体、枚举、VO、搜索抽象、异常体系
├── web/                       # 用户 API（REST + AOP + MyBatis + Scheduling）
│   ├── aspect/                #   AOP 切面（鉴权/日志/监控）
│   ├── controller/            #   REST 控制器
│   ├── service/               #   业务逻辑
│   ├── mapper/                #   MyBatis Mapper
│   └── config/                #   Spring 配置
├── admin/                     # 管理 API（用户管理/视频审核）
├── gateway/                   # 网关（唯一对外入口，lb:// 路由分发）
└── resource/                  # 资源服务（文件 I/O、HLS 转码产物托管，不连业务库）

videoshare-frontend/           # npm workspaces
├── packages/                  # 共享库（api/client/constants/utils）
└── apps/                      # web + admin 双 SPA
```

## 项目概览

全栈视频分享平台（类 YouTube），前后端分离，用户端 + 管理端双 SPA 架构。覆盖从视频上传、HLS 转码、ES 全文检索、点赞评论关注社交链、创作者数据看板，到后台审核管理的完整业务链路。

### 核心功能

- **用户端**：注册登录（图形验证码）→ 视频上传 → HLS 转码 → 发布/搜索(ES) → 播放/点赞/收藏/评论/关注 → 个人主页 → 观看历史 → 通知中心 → 播放列表 → 创作者数据看板
- **管理端**：仪表盘 → 用户管理（封禁/启用）→ 视频审核（发布/驳回/重新上架）→ 评论管理

## 后端能力介绍

### 1. 多服务多模块架构
用户 API（web:7070）、管理 API（admin:7075）、资源服务（resource:7074）独立部署，共享 `common` 模块。common 层封装实体、枚举、VO、搜索抽象、异常体系、工具类，实现业务复用与关注点分离。所有服务注册到 Nacos（8848），由网关（gateway:7071）统一对外、按服务名 `lb://` 负载均衡分发。

### 2. 视频状态机
`VideoStatusEnum` 定义严格状态转换矩阵，`canTransitionTo()` 集中校验所有合法跃迁。消除此前逻辑分散引发的 4 处状态不一致 bug。

| 当前状态 | 合法目标 |
|---------|---------|
| PENDING(0) | PUBLISHED(1) / OFFLINE(2) |
| PUBLISHED(1) | OFFLINE(2) |
| OFFLINE(2) | PUBLISHED(1) |

### 3. AOP 切面体系
| 切面 | 拦截点 | 实现 |
|------|--------|------|
| `PermissionAspect` | `@RequireLogin` 注解 | 提取 Header Token → Redis 校验 → 注入 userId |
| `LogAspect` | 所有 Controller | `@Around` 记录请求/响应/耗时 |
| `MonitorAspect` | Service 层 | 自动记录各方法执行时间 |

### 4. 全文检索（MySQL → ES 迁移）
- `common` 模块中抽象 `VideoSearchService` 接口 + `VideoSearchRepository`（Spring Data Elasticsearch）
- `multiMatchQuery` 实现标题加权（2x），仅检索已发布视频
- 支持相关度/播放量排序，异常静默降级

### 5. FFmpeg 视频转码管线
`VideoTranscoder` 封装 FFmpeg 为外部子进程：
- **硬件加速自动探测**：NVENC → QSV → AMF 优先级检测，无则 fallback `libx264 ultrafast`
- **输出产物**：HLS 分片（`index.m3u8` + `.ts`）+ 截图 + 时长提取
- **健壮性**：独立线程池异步 draining 防死锁，10 分钟超时兜底

### 6. 定时任务调度
`@EnableScheduling` + `@Scheduled`：
- `HeatScoreSyncTask`：每 10 分钟 SQL 重算视频热度分
- `RecommendSyncTask`：周期性预计算推荐数据

### 7. 推荐系统
Redis ZSet 缓存视频相似度（1h TTL），首页按个性化推荐 + 热门兜底混合展示。

### 8. 雪花 ID 生成器
自定义 Snowflake（epoch 2024-01-01），`worker-id` 按模块分配（web=1, admin=2），分布式唯一无冲突。

### 9. 统一异常处理
`ResponseVO<T>` 统一响应体（status/info/data），`BusinessException` 标识业务异常，`GlobalExceptionHandler` 全局兜底。

## Nacos 注册中心与配置中心

所有服务（gateway/web/admin/resource）启动时注册到 Nacos，并从配置中心拉取自身配置。

**1. 启动 Nacos（本地 standalone）**
1. 下载 [nacos-server-2.2.3](https://github.com/alibaba/nacos/releases/tag/2.2.3) 并解压（JDK 8+）
2. Windows 执行 `bin\startup.cmd -m standalone`（Linux/Mac：`bin/startup.sh -m standalone`）
3. 访问控制台 `http://127.0.0.1:8848/nacos`（默认账号 `nacos` / `nacos`）

**2. 导入配置**
在 Nacos 控制台「配置管理 → 配置列表」为每个服务新建一条配置，dataId 与内容见 [DOCS/nacos/configs/](DOCS/nacos/configs/)：

| dataId | 对应服务 |
|--------|---------|
| `gateway-dev.yml` | 网关（lb:// 路由） |
| `web-dev.yml` | 用户 API |
| `admin-dev.yml` | 管理 API |
| `resource-dev.yml` | 资源服务 |

> Nacos 配置中心不可用时，服务自动回退使用本地 `application.yml` 兜底启动（fail-fast=false）；配置中心可用时其配置覆盖本地。

## 快速启动

**环境要求：** JDK 8+、Maven 3、Node.js 18+、MySQL（库 `recordvideo`）、Redis（6379）、Elasticsearch（9200）、FFmpeg、Nacos（见上节）

```bash
# 1. 启动 Nacos（见上节，必须先于各服务）

# 2. 后端（按 gateway → web/admin/resource 顺序）
cd videoshare-backend
mvn clean package -DskipTests
java -jar gateway/target/gateway-1.0.jar   # 网关 7071
java -jar web/target/web-1.0.jar           # 用户 API 7070
java -jar admin/target/videoshare-web-1.0.jar  # 管理 API 7075
java -jar resource/target/resource-1.0.jar # 资源服务 7074

# 3. 前端（浏览器经 gateway:7071 访问后端）
cd videoshare-frontend
npm install && npm run dev                  # web:5173 / admin:3001
```

- 服务注册名统一为 `gateway` / `web` / `admin` / `resource`，可在 Nacos 控制台「服务管理 → 服务列表」查看。
- 前端 API 基址指向网关 `http://127.0.0.1:7071`，由网关按 `lb://` 分发到各服务。
- 切换环境：启动时加 `--spring.profiles.active=test|prod`，服务会拉取 `{服务名}-test.yml` / `{服务名}-prod.yml`。
- 数据库 DDL 见 `web/src/main/resources/iteration3_ddl.sql`。
