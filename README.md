# VideoShare — 视频分享平台

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 + MyBatis + Maven 多模块 |
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
└── admin/                     # 管理 API（用户管理/视频审核）

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

### 1. 双服务多模块架构
用户 API（7075）与管理 API（7070）独立部署、共享 `common` 模块。common 层封装实体、枚举、VO、搜索抽象、异常体系、工具类，实现业务复用与关注点分离。

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

## 快速启动

**环境要求：** JDK 8+、Maven 3、Node.js 18+、MySQL（库 `recordvideo`）、Redis（6379）、Elasticsearch（9200）、FFmpeg

```bash
# 后端
cd videoshare-backend
mvn clean package -DskipTests
java -jar web/target/web-1.0.jar              # 用户 API
java -jar admin/target/videoshare-web-1.0.jar # 管理 API

# 前端
cd videoshare-frontend
npm install && npm run dev                     # web:5173 / admin:3001
```

数据库 DDL 见 `web/src/main/resources/iteration3_ddl.sql`。
