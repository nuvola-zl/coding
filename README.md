# homepage — 个人主页（Java 全栈练手）

Spring Boot + Thymeleaf 的个人主页，前后端不分离。访客不登录可以看主页，修改内容需要管理员登录。

已完成：分层框架、统一响应、全局异常、JWT 登录鉴权、Thymeleaf 页面（主页 / 登录 / 编辑资料 / 作品 / 图片墙）、文件上传（本地 / 阿里云 OSS 双实现）。

## 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | 本机已安装 |
| Spring Boot | 3.5.16 | Web + Validation + Thymeleaf |
| MyBatis-Plus | 3.5.13 | 分页、逻辑删除、自动填充 |
| MySQL | 9.6（本机） | 与 MySQL 8 写法一致，驱动 `mysql-connector-j` |
| jjwt | 0.12.6 | JWT 生成与解析 |
| spring-security-crypto | Boot 管理 | 只用其中的 BCrypt 做密码加密 |
| springdoc-openapi | 2.8.17 | Swagger UI 接口文档 |
| aliyun-sdk-oss | 3.18.5 | 图片上传到阿里云 OSS（没配 key 时自动用本地磁盘） |
| Lombok | - | 简化实体类 |

## 快速开始

1. 初始化数据库

```bash
# 全新环境：建库 + 建表 + 初始数据（profile、sys_user）
mysql -u root -p < sql/init.sql

# 已经建过库，只补某张表：
mysql -u root -p < sql/02-auth.sql     # 用户表
mysql -u root -p < sql/03-project.sql  # 作品表
mysql -u root -p < sql/04-gallery.sql  # 图库表
```

2. 检查 `src/main/resources/application-dev.yml` 里的 `username` / `password` 是否与本地 MySQL 一致。

3. 启动

```bash
mvn spring-boot:run
# 或直接运行 HomepageApplication
```

4. 打开 <http://localhost:8080/> 就是主页。

5. 站点账号：`linwanqing`（管理员，第一个注册的账号自动成为 ADMIN）。
   浏览器登录后，Cookie 会存下登录态（HttpOnly），7 天内免登录。

接口文档：<http://localhost:8080/swagger-ui.html>（调试需要权限的接口时，点右上角 Authorize 粘贴 token）。

## 页面

| 路径 | 说明 | 权限 |
| --- | --- | --- |
| `/` | 主页：头像、昵称、简介、关于我、联系方式、作品（最多 6 个） | 公开 |
| `/login` | 登录页（带表单校验和错误提示） | 公开 |
| `/edit` | 编辑资料表单 | 管理员 |
| `/projects` | 作品列表（分页，每页 6 个） | 公开 |
| `/projects/{id}` | 作品详情；草稿按 404 处理 | 公开 |
| `/gallery` | 图片墙（分类 Tab + 大图预览 + 分页） | 公开 |
| `/admin/projects` | 作品管理（含草稿） | 管理员 |
| `/admin/projects/new` | 新建作品 | 管理员 |
| `/admin/projects/{id}/edit` | 编辑作品 | 管理员 |
| `/admin/gallery` | 图片管理（含草稿） | 管理员 |
| `/admin/gallery/new` | 上传图片 | 管理员 |
| `/admin/gallery/{id}/edit` | 编辑图片（可不换图只改文字） | 管理员 |

交互规则：

- 主页右上角未登录显示「登录」，已登录显示「退出登录」。
- 主页的「编辑资料」按钮对所有人可见；未登录点击会跳到登录页，并带上 `?redirect=/edit`，登录成功后自动回到编辑页。
- **登录过期是静默的**：token 过期后访问主页一切正常（公开内容），只有点「编辑资料」这类写操作时才会被要求登录。
- 保存成功后回到主页并显示「资料已保存」提示。

## 内容模块：作品（project）

内容模块和 `profile` 这类配置表的本质区别：**profile 全站只有 1 条记录，只做查和改；内容表有 N 条记录，要有列表、详情、分页和发布状态**。

### 数据模型

`project` 表核心字段：

| 字段 | 说明 |
| --- | --- |
| `title` / `summary` / `description` | 名称 / 一句话简介（列表页）/ 详细介绍（详情页，支持换行） |
| `cover` | 封面图 URL，留空则用项目名首字做占位 |
| `tech_stack` | 技术栈，**逗号分隔的字符串**。存英文逗号，展示时拆成数组 |
| `repo_url` / `demo_url` / `period` | 仓库、演示、时间区间 |
| `sort_order` | 排序，越小越靠前 |
| `status` | `DRAFT` 草稿 / `PUBLISHED` 已发布 |

设计决策：

- **草稿/发布**：主页和列表页只查 `PUBLISHED`，草稿被猜 id 访问也按 404 处理；所有内容模块共用这套状态。
- **标签先存字符串**：等需要"按标签筛选/标签云"时再拆关联表，避免过早设计。
- **技术栈统一英文逗号**：入库时把中文逗号归一化，避免页面上少一个标签。

### 页面

- 主页「作品」区块：已发布、按 `sort_order` 排序、最多 6 个，带「全部作品 →」入口。
- `/projects`：分页列表（上一页/下一页）。
- `/projects/{id}`：详情页；不存在或未发布渲染 `error/404.html`，HTTP 404。
- `/admin/projects`：管理列表（含草稿），新建 / 编辑 / 删除（逻辑删除，`deleted=1`）。

### 已预置的示例数据

`sql/init.sql` 和 `03-project.sql` 里插了 3 个作品（苍穹外卖、黑马点评、个人主页），可以直接在后台改掉。

## 图片模块（gallery）与文件存储

### 图库

- 两个分类：`PERSONAL` 个人喜欢 / `PROJECT` 项目图片（项目图片可关联到某个作品）。
- `/gallery` 图片墙：分类 Tab（带数量徽标）+ CSS 多列瀑布流 + 点开大图（纯原生 JS，支持 ESC 关闭、左右键切换）。
- 上传走后台 `/admin/gallery/new`，和作品一样有 `DRAFT / PUBLISHED` 状态。
- **和作品的两种关联**（互不冲突，按需选用）：
  - `project.cover`：作品的**单张封面**，在后台编辑作品时填图片地址，列表卡片和详情页顶部会显示；
  - `gallery.project_id`：图片**多对一**挂到作品下，在后台编辑图片时选「关联作品」，作品详情页底部会出现「项目截图」区（草稿不显示）。
- 删除时记录逻辑删除，**文件实体同步删除**（省存储费；想支持"回收站"就去掉 `GalleryServiceImpl#deleteGallery` 里的删文件一行）。

### 存储层：本地 / 阿里云 OSS 双实现

业务代码只依赖 `StorageService` 接口，具体用哪套由配置决定：

| 情况 | 行为 |
| --- | --- |
| 没填 OSS 配置（默认） | 图片存 `./uploads/`，通过 `/uploads/**` 访问，零依赖直接跑 |
| 填了 OSS 四项必填 | 自动切换到阿里云 OSS，返回 `https://bucket.endpoint/key` 地址 |

**key 在哪配：`config/application-local.yml`（本机真实配置，已被 .gitignore 忽略，不进 Git）**。
新机器上先复制模板再填：

```bash
cp config/application-local.yml.example config/application-local.yml
```

```yaml
app:
  storage:
    aliyun:
      endpoint: ${OSS_ENDPOINT:oss-cn-hangzhou.aliyuncs.com}   # Bucket 概览页的「地域节点」
      access-key-id: ${OSS_ACCESS_KEY_ID:你的AccessKeyId}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET:你的AccessKeySecret}
      bucket: ${OSS_BUCKET:你的Bucket名}
      domain: ${OSS_DOMAIN:}                                    # 可选：自定义/CDN 域名
```

## 配置里的密码 / 密钥怎么保管（占位符机制）

**原则：能提交到 Git 的文件里不能有真实密钥。** 这个项目的分层是：

| 文件 | 是否进 Git | 放什么 |
| --- | --- | --- |
| `application.yml` / `application-dev.yml` | 进 | 普通配置；密钥处只留**占位符**（如 `${OSS_ACCESS_KEY_ID:}`） |
| `config/application-local.yml` | **不进**（.gitignore 已忽略） | 本机真实密码、AccessKey |
| `config/application-local.yml.example` | 进 | 模板，全是假的占位值，教别人怎么填 |

加载原理：`application.yml` 里有一行
`spring.config.import: optional:file:./config/application-local.yml`，
它把本地真实配置叠加进来。`optional:` 表示文件不存在也能启动 ——
此时 OSS 四项缺失，程序自动回退到本地磁盘存储；数据库密码用的是
`${DB_PASSWORD:默认值}`，同样是环境变量优先。

**优先级（从高到低）：命令行参数 > 环境变量 > application-local.yml > application.yml。**

所以同一份代码有三种用法，一行都不用改：

1. **本机开发**：什么都不设，用 application-local.yml 里的值；
2. **IDEA 里跑**：Run Configuration → Environment variables 里填 `OSS_ACCESS_KEY_ID=xxx;...`，优先级高于文件；
3. **部署到服务器**：不传 application-local.yml，用 systemd 的 `Environment=` 或 Docker 的 `-e` 注入。

怎么确认占位符真的被填上了？看启动日志：
`阿里云 OSS 已启用 bucket=xxx endpoint=xxx` = 本地配置已加载；
`图片存储使用【本地磁盘】` = 没加载到（或没配）。

安全清单：

- AccessKey 用 **RAM 子账号**创建，权限只授予这一个 Bucket 的读写，绝不用主账号 AK；
- 密钥一旦进过 Git 历史（哪怕后来删了）就算泄露，要去控制台**禁用并换新的**；
- `application-local.yml` 也可以放数据库密码等任何敏感配置，格式见 example 文件。

上传的安全校验（`UploadServiceImpl` + `ImageType`）：

- **按文件头魔数判型**，不看扩展名 —— 把 `.jsp` 改名成 `.jpg` 会被直接拒绝；
- 白名单只放 JPG / PNG / GIF / WEBP / BMP，单张上限 5MB；
- 上传成功才落库，替换头像时旧文件同步删除。

### 排障：图片上传成功了，但页面上显示不出来

**现象**：后台能看到图片记录、OSS 控制台里文件也在，但主页 / 图片墙是一片空白（图片图标裂开）。

**原因**：Bucket 的读写权限是「私有」。上传时程序带着 AccessKey 签名，所以能写进去；
但浏览器渲染 `<img src="https://bucket.xxx/key">` 是**匿名请求、不带任何签名**，OSS 直接返回：

```xml
HTTP/1.1 403 Forbidden
<Code>AccessDenied</Code>
<Message>You have no right to access this object because of bucket acl.</Message>
```

本地存储没有这个问题，因为 `/uploads/**` 是 Spring Boot 自己的静态资源映射，不经过 OSS 鉴权——
所以「本地正常、OSS 不正常」几乎一定是这个原因。

**修复**（改配置，不用改代码、不用重传、不用重启）：

1. OSS 控制台 → Bucket 列表 → 你的 Bucket → **权限控制** → 读写权限 → 改为 **公共读**；
2. 同一页如果「**阻止公共访问**」是开启状态，**先关掉**，否则第 1 步不生效；
3. 刷新页面即可。

**怎么快速确认**：拿库里存的地址直接在浏览器或命令行访问，能出图就是通的：

```bash
curl -I "https://你的bucket.oss-cn-hangzhou.aliyuncs.com/homepage/gallery/2026/11/08/xxxx.jpg"
```

程序也会帮你查：每次上传成功后会用**匿名 HEAD 请求**探一次地址，访问不通就在日志里打 WARN 并写明原因
（见 `AliyunOssStorageService#checkAnonymousReadable`）。只告警不阻断——图片确实传成功了，不该因为权限配置让上传流程失败。

**如果不想把 Bucket 设成公共读**：那就得改用「预签名 URL」（`generatePresignedUrl`，带过期时间），
数据库里存对象 key 而不是完整地址，渲染时实时签发。安全性更好，但地址会过期、无法被 CDN 长期缓存，
个人主页这种全公开的图片没必要这么做。

### 上传相关接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/upload/image` | 管理员 | 通用图片上传，multipart 字段 `file`，返回 `{url, storage, ...}` |
| POST | `/api/profile/avatar` | 管理员 | 上传头像并直接写回个人信息 |
| POST | `/edit/avatar` | 管理员 | 编辑页的表单版头像上传 |

## 认证与权限

### 设计原则

**读接口/页面全公开，写操作要登录。**

### 角色

| 角色 | 来源 | 能力 |
| --- | --- | --- |
| `ADMIN` | 系统内**第一个**注册的账号 | 浏览 + 修改站点内容 |
| `USER` | 之后注册的账号 | 浏览 + 登录，不能修改内容 |

### 怎么用（后端视角）

接口上标 `@LoginRequired` 即生效，默认登录即可访问；`@LoginRequired(minRole = Role.ADMIN)` 要求管理员。

```java
@LoginRequired(minRole = Role.ADMIN)
@PutMapping
public Result<ProfileVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) { ... }
```

### 实现方式

- `LoginInterceptor` 先尝试解析 token（`Authorization` 头或 Cookie），解析成功就把用户放进 `UserContext`（ThreadLocal）—— 这一步不强制，所以公开页面也知道"当前是谁"；然后再校验 `@LoginRequired`。
- 拒绝时按请求类型分流：`/api/**` 返回 JSON（401/403），页面 302 跳登录页并记住原地址。
- 前后端不分离阶段，登录后 token 写入 **HttpOnly Cookie**（JS 读不到，防 XSS 偷 token）；以后拆前后端分离，前端改用 `Authorization: Bearer <token>` 头即可，token 格式不变。
- 密码用 BCrypt 存储，不可逆；登录时 `matches(明文, 密文)` 比对。
- JWT 无状态，退出登录 = 前端删 Cookie，服务端不维护会话。

### 注意事项

- `jwt.secret` 在 `application.yml`，HS256 要求至少 32 字节，**上线前必须换随机串并用环境变量注入**。
- 注册接口目前开放（`POST /api/auth/register`），不想让人注册时在 `AuthServiceImpl.register` 里加个开关即可。

## 目录结构

```
homepage
├── sql/
│   ├── init.sql                        建库建表（profile + sys_user + project）+ 初始数据
│   ├── 02-auth.sql                     增量脚本：只建 sys_user 表
│   └── 03-project.sql                  增量脚本：只建 project 表
├── src/main/java/com/zhanglin/homepage
│   ├── HomepageApplication.java        启动类（@MapperScan 扫 mapper 包）
│   ├── sql/init.sql 同级：02-auth / 03-project / 04-gallery 增量脚本
│   ├── common/                         Result / ResultCode / PageResult / PageQuery / 全局异常 / 枚举
│   ├── security/                       认证相关
│   │   ├── JwtUtils.java               生成 / 解析 token
│   │   ├── JwtProperties.java          jwt.* 配置绑定
│   │   ├── LoginInterceptor.java       身份解析 + 登录/角色校验 + 页面跳转分流
│   │   ├── LoginRequired.java          权限注解
│   │   ├── LoginUser.java / Role.java  当前用户 / 角色枚举
│   │   ├── UserContext.java            ThreadLocal 上下文
│   │   └── TokenCookieUtils.java       登录态 Cookie 读写
│   ├── service/storage/                存储抽象层
│   │   ├── StorageService.java         接口：upload / deleteQuietly / type
│   │   └── impl/AliyunOssStorageService.java   阿里云实现
│   │   └── impl/LocalStorageService.java       本地磁盘实现
│   ├── config/                         MybatisPlus / 自动填充 / Jackson / WebMvc / Password / OpenApi / Storage
│   ├── controller/
│   │   ├── PageController.java         页面路由（/ /login /logout /edit /edit/avatar）
│   │   ├── ProjectController.java      作品页面（/projects，/projects/{id}）
│   │   ├── AdminProjectController.java 作品后台（/admin/projects/**）
│   │   ├── GalleryController.java      图片墙（/gallery）
│   │   ├── AdminGalleryController.java 图库后台（/admin/gallery/**）
│   │   ├── UploadController.java       上传接口（/api/upload/**）
│   │   ├── AuthController.java         认证接口（/api/auth/**）
│   │   ├── ProfileController.java      个人信息接口（/api/profile，含 /avatar）
│   │   └── HealthController.java       健康检查
│   ├── service/ + service/impl/        业务层
│   ├── mapper/                         Mapper 接口（继承 BaseMapper）
│   ├── entity/                         Profile / SysUser / Project / Gallery（BaseEntity 抽公共字段）
│   ├── dto/ vo/                        入参 / 出参
└── src/main/resources
    ├── application.yml                 公共配置 + jwt + thymeleaf
    ├── application-dev.yml             开发环境（数据源、SQL 日志）
    ├── mapper/ProfileMapper.xml        自定义 SQL
    ├── templates/
    │   ├── fragments/layout.html       公共头尾片段（th:replace 复用）
    │   ├── index.html                  主页
    │   ├── login.html                  登录页
    │   ├── edit.html                   编辑资料
    │   ├── projects.html               作品列表
    │   ├── project.html                作品详情
    │   ├── error/404.html              404 页
    │   └── admin/                      作品管理列表 + 表单
    └── static/                         css/site.css、favicon.svg
```

## 接口约定

- HTTP 状态码恒为 200，业务结果看响应体 `code`：`200` 成功，其它失败。
- 状态码：`400` 参数错误、`401` 未登录/登录失效、`403` 无权限、`404` 资源不存在、`405` 方法不支持、`600` 业务异常、`500` 系统异常。
- 前端收到 `401` 清掉本地 token 跳登录页。

## 全部路由

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/` | 公开 | 主页 |
| GET | `/login` | 公开 | 登录页 |
| POST | `/login` | 公开 | 表单登录，成功写 Cookie |
| POST | `/logout` | 公开 | 退出登录，清 Cookie |
| GET | `/edit` | 管理员 | 编辑资料页 |
| POST | `/edit` | 管理员 | 保存资料 |
| GET | `/projects` | 公开 | 作品列表（分页） |
| GET | `/projects/{id}` | 公开 | 作品详情 |
| GET | `/admin/projects` | 管理员 | 作品管理 |
| GET | `/admin/projects/new` | 管理员 | 新建作品页 |
| POST | `/admin/projects/new` | 管理员 | 保存新作品 |
| GET | `/admin/projects/{id}/edit` | 管理员 | 编辑作品页 |
| POST | `/admin/projects/{id}/edit` | 管理员 | 保存修改 |
| POST | `/admin/projects/{id}/delete` | 管理员 | 删除作品（逻辑删除） |
| GET | `/api/health` | 公开 | 健康检查 |
| GET | `/api/profile` | 公开 | 查询个人信息 |
| PUT | `/api/profile` | 管理员 | 修改个人信息 |
| POST | `/api/profile/avatar` | 管理员 | 上传头像（multipart） |
| POST | `/api/upload/image` | 管理员 | 通用图片上传（multipart） |
| GET | `/gallery` | 公开 | 图片墙 |
| GET | `/admin/gallery` | 管理员 | 图片管理 |
| GET | `/admin/gallery/new` | 管理员 | 上传图片页 |
| POST | `/admin/gallery/new` | 管理员 | 保存新图片（multipart） |
| GET | `/admin/gallery/{id}/edit` | 管理员 | 编辑图片页 |
| POST | `/admin/gallery/{id}/edit` | 管理员 | 保存修改（file 可空 = 不换图） |
| POST | `/admin/gallery/{id}/delete` | 管理员 | 删除图片（记录 + 文件） |
| POST | `/api/auth/register` | 公开 | 注册 |
| POST | `/api/auth/login` | 公开 | 登录（JSON，返回 token） |
| GET | `/api/auth/me` | 登录 | 当前登录用户 |
| POST | `/api/auth/logout` | 登录 | 退出登录 |

## 开发约定

- 新增页面：`PageController` 加路由 → `templates/` 加模板 → 复用 `css/site.css`。
- 新增内容模块（如技能、文章）照作品模块复制即可：`entity → mapper → service → controller（公开页面 + /admin 管理页面）`，状态字段直接复用 `ProjectStatus` 的思路或抽成通用枚举。
- 单表增删改查直接用 `BaseMapper` / `IService`，复杂 SQL 写进 `resources/mapper/*.xml`。
- 业务校验失败抛 `BusinessException`；写接口记得加 `@LoginRequired` 并按需指定 `minRole`。
- 逻辑删除字段 `deleted` 在 `BaseEntity` 中处理；自定义 XML 里的 SQL 要自己加 `deleted = 0`。
- 分页查询用 `PageQuery#toPage()` + `PageResult#of()`。

## 待确认 / 后续规划

- [ ] 内容模块扩展：技能（简单）→ 文章（要先定 Markdown 渲染方案）→ 时间线
- [ ] 文章是否需要 slug（/article/hello-world）
- [x] 文件上传：已做双实现（本地 / 阿里云 OSS），在 `app.storage` 配置切换
- [ ] 修改密码功能
- [ ] 注册开关：现在注册接口是开放的，需要时关闭
- [ ] 未匹配 URL 目前返回 JSON 404，浏览器访问时统一渲染 error/404.html
- [ ] 以后拆前后端分离时：登录接口改造成纯 JSON + token 放 localStorage
