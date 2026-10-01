# 重构检查清单（Quick Reference）

## ✅ 已完成（P0-P1）

- [x] **72b0c00** 修复 getHlsDir() 路径构建 bug
- [x] **3efaead** 点赞/收藏分布式锁 + 原子更新  
- [x] **69ad3fe** ES 索引重建逐条重试模式
- [x] **0c8292b** ES 异步降级避免阻塞事务

---

## 🔥 Level 1: 紧急修复（本周执行）

### [ ] 网关认证空指针防护
```bash
# 需要检查的文件
videoshare-backend/gateway/src/main/java/com/videoshare/gateway/filter/*.java

# 关键检查点
✓ GlobalAuthFilter - 检查 serverRequest 是否为 null
✓ AdminAuthGatewayFilterFactory - token 解析异常处理
✓ UserAuthGatewayFilterFactory - 非法 token 的优雅降级
```
**工时**: 2h | **优先级**: P0

---

### [ ] 数据库连接池优化
```bash
# 修改位置
Nacos 配置中心 / application.yml
或 videoshare-backend/*/src/main/resources/application.yml

# 建议配置
spring.datasource.hikari.maximum-pool-size: 50
spring.datasource.hikari.minimum-idle: 10
```
**工时**: 1h | **优先级**: P1

---

## 🏗️ Level 2: 架构优化（下周执行）

### [ ] Service 层事务边界审查
```bash
# 需要扫描的目录
videoshare-backend/web/src/main/java/com/videoshare/web/service/impl/*.java

# 重点方法
✓ VideoServiceImpl.toggleAction()
✓ CommentServiceImpl.delete()
✓ NotificationServiceImpl.sendNotification()
```
**工时**: 3h | **优先级**: P1

---

### [ ] N+1 查询问题修复
```bash
# 识别模式
for file in $(grep -r "selectByUserId" videoshare-backend --include="*.java"); do
    echo "$file"
done

# 替代方案
userInfoMapper.selectBatchIds(userIds)
```
**工时**: 2h | **优先级**: P2

---

## 🧹 Level 3: 代码质量（持续进行）

### [ ] 魔法值消除
```bash
# 查找魔术数字
grep -rn "== 1\|== 2\|== 0" videoshare-backend/src/main --include="*.java" | grep -v enum | grep -v test

# 查找魔术字符串
grep -rn "LIKE.*keyword\|status.*==" videoshare-backend/src/main --include="*.java"
```
**工时**: 4h | **优先级**: P2

---

### [ ] 日志规范统一
```bash
# 检查遗漏上下文的日志
grep -rn "log.error.*," videoshare-backend/src/main --include="*.java" | grep -v "{}"

# 修复示例
// ❌ log.error("同步失败", e);
// ✅ log.error("同步视频 {} 到 ES 失败，userId={}, error={}", videoId, userId, e.getMessage(), e);
```
**工时**: 2h | **优先级**: P3

---

### [ ] 常量集中管理
```bash
# 创建文件
videoshare-backend/common/src/main/java/com/videoshare/common/constants/CacheKeys.java

# 内容模板
public interface CacheKeys {
    String VIDEO_LIKE_PREFIX = "video:like:";
    String VIDEO_FAV_PREFIX = "video:fav:";
    long DEFAULT_TTL_MS = 1000L * 60 * 60; // 1 小时
}
```
**工时**: 3h | **优先级**: P2

---

## 🎨 Frontend (并行执行)

### [ ] TypeScript Strict 模式
```bash
# 修改 tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true
  }
}

# 运行检查
cd videoshare-frontend
npm run build  # 查看类型错误
```
**工时**: 4-6h | **优先级**: P2

---

### [ ] ESLint 深度扫描
```bash
# 安装依赖
npm install -D @typescript-eslint/eslint-plugin@latest

# 增强规则 (.eslintrc.js)
rules: {
  '@typescript-eslint/no-explicit-any': 'error',
  'max-depth': ['error', 4],
  'complexity': ['error', 20]
}

# 运行检查
npm run lint -- --fix
```
**工时**: 3h | **优先级**: P3

---

### [ ] API Client 类型完整
```bash
# 检查文件
videoshare-frontend/packages/api/client/*.ts

# 修复示例
interface ICreateVideoReq {
  title: string;
  description?: string;
  category: VideoCategory;
}
```
**工时**: 4h | **优先级**: P2

---

## 📊 PR Checklist（每次提交前）

- [ ] **功能正确性**
  - [ ] 单元测试通过？
  - [ ] 集成测试通过？
  - [ ] 边界条件已考虑？

- [ ] **代码质量**
  - [ ] 无魔法值？
  - [ ] 日志上下文完整？
  - [ ] 异常处理适当？

- [ ] **性能影响**
  - [ ] 是否有 N+1 查询？
  - [ ] 是否使用了正确的索引？
  - [ ] Redis 操作有超时保护？

- [ ] **文档**
  - [ ] 注释清晰？
  - [ ] README 已更新（如适用）？
  - [ ] CHANGELOG 已添加条目？

---

## 🔧 快速命令参考

### 后端静态分析
```bash
cd videoshare-backend
mvn spotbugs:check              # 空指针、资源泄漏检测
mvn pmd:pmd                     # 代码规范检查
mvn dependency:tree -Dverbose   # 依赖冲突检查
```

### 前端类型检查
```bash
cd videoshare-frontend
npx tsc --noEmit                # TypeScript 编译检查
npm run lint                    # ESLint 检查
```

### 批量搜索
```bash
# 查找所有 TODO
find . -name "*.java" -exec grep -l "TODO" {} \;
find . -name "*.ts" -exec grep -l "TODO" {} \;

# 查找所有FIXME
grep -rn "FIXME" videoshare-backend/src/main --include="*.java"
```

---

## 🎯 目标里程碑

| 阶段 | 完成时间 | 达成标准 |
|------|---------|---------|
| Phase 1 ✅ | Day 1 | P0-P1 全部完成 |
| Phase 2 | Day 3 | 网关 + 数据库优化完成 |
| Phase 3 | Day 5 | 代码质量显著提升 |
| Phase 4 | Day 7 | 前后端全面加固 |

---

> **提示**: 此清单用于快速参考，详细计划请查阅 [`CODE-REFACTORING-GUIDE.md`](./CODE-REFACTORING-GUIDE.md)
