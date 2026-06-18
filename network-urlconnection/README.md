# network-urlconnection

将 Android 系统级 `HttpURLConnection` / `URL.openConnection()` 代理到 OkHttp，便于统一网络栈、接入 Flipper/Chucker 等调试能力。

本模块在全局 `OkUrlFactory` 代理基础上，增加了**域名白名单 bypass**：部分域名仍走系统原生 `HttpURLConnection`，避免与 OkHttp URLConnection 桥接层不兼容的 SDK 请求卡死。

## 背景

部分第三方 SDK（如神策 Analytics）通过 `HttpURLConnection` 上报数据，且使用如下模式：

- `setFixedLengthStreamingMode()` 流式写 body
- POST body 为 `application/x-www-form-urlencoded`（`crc` / `gzip` / `data_list` 表单参数）
- `setInstanceFollowRedirects(false)` 手动处理重定向

`OkUrlFactory` 将 `HttpURLConnection` 桥接到 OkHttp 时，对上述模式存在兼容性问题，可能导致 `getResponseCode()` 长时间阻塞、上报无响应。

**解决方案**：按域名白名单分流——白名单域名走系统原生连接，其余走 OkHttp。

## 工作原理

```
URL.openConnection()
        │
        ▼
WhitelistUrlStreamHandlerFactory
        │
        ├─ host 命中白名单 ──► SystemHandlerHolder（系统原生 HttpURLConnection）
        │
        └─ 其他 host ────────► OkUrlFactory ──► OkHttp
```

## 快速接入

### 1. 添加依赖

```gradle
implementation project(':network-urlconnection')
// 或
implementation 'io.silvrr.base:xxx' // 按实际发布坐标
```

模块已通过 `ContentProvider`（`ProxyUrlInit2`，`initOrder=200`）自动初始化，一般无需手动调用。

### 2. 配置 bypass 域名（必做）

库内**不内置**任何业务域名，需在宿主 App 中配置。

#### 方式 A：AndroidManifest meta-data（推荐）

在宿主 `AndroidManifest.xml` 的 `<application>` 下添加：

```xml
<meta-data
    android:name="flipper.urlconnection.bypass_exact_hosts"
    android:value="sc.example.com,sc.other.com" />

<!-- 可选：追加后缀匹配，默认已内置 .sensorsdata.cn -->
<meta-data
    android:name="flipper.urlconnection.bypass_suffix_hosts"
    android:value=".sensorsdata.cn" />
```

多个域名用英文逗号分隔。

#### 方式 B：代码配置

须在 `URL.setURLStreamHandlerFactory` 执行之前调用（早于任何网络请求）：

```java
BypassHostConfig.addExactHost("sc.example.com");
BypassHostConfig.addSuffixHost(".sensorsdata.cn");

ProxyUrlConnectionUtil.proxyUrlConnection(context);
```

方式 A 与 B 可混用，配置会合并。

### 3. 验证

Logcat 过滤 `WhitelistUrlFactory`，白名单请求会看到：

```
bypass system handler: https://sc.example.com/sa?project=production
```

## 白名单匹配规则

| 类型 | 说明 | 示例 |
|------|------|------|
| 精确匹配 | 完整 host 相等（忽略大小写） | `sc.example.com` |
| 后缀匹配 | host 以指定后缀结尾（后缀须以 `.` 开头） | `.sensorsdata.cn` → `sa.sensorsdata.cn` |

默认内置后缀：`.sensorsdata.cn`

## API 参考

### BypassHostConfig

| 方法 | 说明 |
|------|------|
| `addExactHost(String)` | 添加精确匹配域名 |
| `addExactHosts(Collection<String>)` | 批量添加精确域名 |
| `removeExactHost(String)` | 移除精确域名 |
| `clearExactHosts()` | 清空精确域名 |
| `addSuffixHost(String)` | 添加后缀（自动补前导 `.`） |
| `addSuffixHost(Collection<String>)` | 批量添加后缀 |
| `removeSuffixHost(String)` | 移除后缀 |
| `resetSuffixHostsToDefault()` | 重置后缀为默认 `.sensorsdata.cn` |

### BypassHostManifestReader

| 常量 | 说明 |
|------|------|
| `META_EXACT_HOSTS` | `flipper.urlconnection.bypass_exact_hosts` |
| `META_SUFFIX_HOSTS` | `flipper.urlconnection.bypass_suffix_hosts` |

| 方法 | 说明 |
|------|------|
| `apply(Context)` | 读取 manifest 配置并写入 `BypassHostConfig` |

### ProxyUrlConnectionUtil

| 方法 | 说明 |
|------|------|
| `proxyUrlConnection()` | 安装全局 URLStreamHandlerFactory |
| `proxyUrlConnection(Context)` | 同上，并自动读取 manifest meta-data |

### ProxyUrlInterceptor

通过 `OkhttpAspect` 切面，为来自 URLConnection 桥接的 OkHttp 请求添加 `flipper-fromUrlConnection: 1` 标记头，便于 Flipper 等工具识别来源。

## 模块结构

```
com.hss01248.flipper.urlconnection/
├── BypassHostConfig.java              # 白名单配置 API
├── BypassHostManifestReader.java      # 从 manifest 读取白名单
├── HostMatcher.java                   # 域名匹配逻辑
├── SystemHandlerHolder.java           # 缓存系统默认 URLStreamHandler
├── WhitelistUrlStreamHandlerFactory.java  # 分流 Factory
├── ProxyUrlConnectionUtil.java        # 入口：安装全局代理
├── ProxyUrlInterceptor.java           # OkHttp 标记拦截器
├── InitForUrlConnection.java          # Startup Initializer
└── ProxyUrlInit2.java                 # ContentProvider 自动初始化
```

## 注意事项

### OkHttpClient 拦截器无效

`OkHttpURLConnection.buildCall()` 会清空 `OkHttpClient.Builder` 上配置的拦截器。若需对 URLConnection 代理流量加拦截器，请通过 `network-hook` 模块的 `OkhttpAspect.addHook()` 在 `beforeBuild` 中添加（参考 `InitForUrlConnection`）。

### setURLStreamHandlerFactory 只能调用一次

全局 Factory 一旦被其他库设置，本模块调用会失败（异常被捕获并忽略）。请确保初始化顺序正确。

### 初始化时机

`ProxyUrlInit2` 在 `ContentProvider.onCreate` 中执行，早于 `Application.onCreate`。因此：

- **推荐**通过 manifest `meta-data` 配置白名单
- 若用代码配置，须在 `initOrder` 更小的 `ContentProvider` / `Initializer` 中提前调用 `BypassHostConfig`

### minSdk

`minSdkVersion 19`，系统 Handler 通过反射加载，已兼容 API 19–34 的常见实现：

- API 24+：`com.android.okhttp.HttpHandler` / `HttpsHandler`
- API 19–23：`libcore.net.http.HttpHandler` / `HttpsHandler`

## 依赖

| 依赖 | 用途 |
|------|------|
| `okhttp-urlconnection:3.12.12` | URLConnection → OkHttp 桥接 |
| `network-hook` | OkhttpAspect 切面 |
| `androidx.startup` | Initializer 支持 |

## 常见问题

**Q: 神策上报卡住、无 responseCode 日志？**

确认上报域名已加入 `bypass_exact_hosts` 或 `bypass_suffix_hosts`，且 logcat 能看到 `bypass system handler`。

**Q: 配置了白名单但仍走 OkHttp？**

检查 meta-data 是否写在宿主 App 的 `AndroidManifest.xml`（非 library manifest 合并遗漏），或域名拼写/大小写是否正确。

**Q: 能否按路径（如 `/sa`）匹配？**

当前仅支持域名精确/后缀匹配。若有需求可扩展 `HostMatcher`。

## License

与 FlipperDemo 主工程保持一致。
