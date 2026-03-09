# Network 模块

本模块是对 Facebook Flipper Network 插件的二次开发，核心类为 `FlipperOkhttpInterceptor`，用于在 Flipper 桌面端查看 OkHttp 网络请求/响应的详细信息。

## 模块文件概览

| 文件 | 职责 |
|---|---|
| `FlipperOkhttpInterceptor.java` | OkHttp 拦截器主逻辑，拦截请求/响应并上报给 Flipper |
| `NetworkFlipperPlugin.java` | Flipper 插件，负责将 Request/Response 数据发送到 Flipper 桌面端 |
| `NetworkReporter.java` | 数据模型定义（`RequestInfo`、`ResponseInfo`、`Header`） |
| `NetworkResponseFormatter.java` | 响应格式化器接口 |
| `RequestBodyParser.java` | 自定义请求体解析器接口（用于加密场景的解密展示） |

---

## FlipperOkhttpInterceptor 核心逻辑

### 类签名

```java
public class FlipperOkhttpInterceptor
    implements Interceptor, BufferingFlipperPlugin.MockResponseConnectionListener
```

同时实现了：
- `okhttp3.Interceptor`：OkHttp 拦截器
- `MockResponseConnectionListener`：Mock 响应的连接监听（从 Flipper 桌面端接收 mock 配置）

### 关键字段

| 字段 | 说明 |
|---|---|
| `DEFAULT_MAX_BODY_BYTES` | 默认 body 最大读取大小：**1MB**，防止 OOM |
| `mMaxBodyBytes` | 实际使用的 body 大小限制（可通过构造函数自定义） |
| `mPlugin` | `NetworkFlipperPlugin` 实例，用于上报数据 |
| `mMockResponseMap` | Mock 响应映射表，key 为 `(url, method)` |
| `mIsMockResponseSupported` | 是否启用 Mock 响应功能 |
| `requestBodyParser` | 静态字段，自定义请求体解析器 |

### 构造函数

提供 4 个构造函数，最终都委托到全参数版本：

```java
FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin)                                     // 默认 1MB，不支持 mock
FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin, long maxBodyBytes)                   // 自定义大小，不支持 mock
FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin, boolean isMockResponseSupported)     // 默认 1MB，可选 mock
FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin, long maxBodyBytes, boolean isMockResponseSupported)  // 全参数
```

> 若启用 mock (`isMockResponseSupported=true`)，需使用 `addInterceptor`（应用拦截器）而非 `addNetworkInterceptor`，以支持短路返回。

---

### `intercept()` 主流程

这是拦截器的核心方法，完整流程如下：

```
┌──────────────────────────────────────────────────────┐
│               intercept(chain)                       │
├──────────────────────────────────────────────────────┤
│                                                      │
│  1. 获取 request                                     │
│  2. cloneBodyAndInvalidateRequest() 克隆请求体       │
│  3. 生成 UUID 作为 identifier                        │
│  4. convertRequest() → mPlugin.reportRequest()       │
│     上报请求信息到 Flipper                            │
│  5. removeDebugHeaders() 移除调试 header              │
│  6. 检查是否有 mock 响应                              │
│                                                      │
│  ┌─ try ─────────────────────────────────────────┐   │
│  │                                               │   │
│  │  执行请求 (mock 或真实)                         │   │
│  │                                               │   │
│  │  统一 Tee 模式:                                │   │
│  │  wrapResponseWithTee() 包装响应体              │   │
│  │  立即返回,不阻塞                               │   │
│  │  body 在调用方消费时 copyTo 一份给 Flipper      │   │
│  │                                               │   │
│  ├─ catch (Throwable) ───────────────────────────┤   │
│  │                                               │   │
│  │  构造 499 错误响应（包含异常堆栈）              │   │
│  │  上报错误信息到 Flipper                        │   │
│  │  重新抛出异常（包装为 IOException）             │   │
│  │                                               │   │
│  └───────────────────────────────────────────────┘   │
│                                                      │
└──────────────────────────────────────────────────────┘
```

#### 异常处理细节

当请求抛出异常时，拦截器不会吞掉异常，而是：
1. 将异常堆栈转为字符串
2. 构造一个 **HTTP 499** 伪响应（包含 `exception`、`msg`、`Date` 等 header）
3. 将这个伪响应上报到 Flipper（方便在桌面端看到错误详情）
4. 重新抛出原始异常（如果是 `IOException` 直接抛，否则包装为 `IOException`）

---

### 请求处理

#### `cloneBodyAndInvalidateRequest(request, bodyDesc)`

克隆请求体，确保 body 可以被多次读取。

- **body < 1MB**：完整克隆 body buffer，构建新的 Request 对象
- **body ≥ 1MB**：不克隆原始 body，改为通过 `MyAppHelperInterceptor.getRequestBodyMeta()` 提取元数据，序列化为 JSON 写入 buffer（避免 OOM）
- **body 为 null**：返回 `(request, null)`

#### `convertRequest(request, bodyBuffer, identifier)`

将 OkHttp Request 转换为 `RequestInfo` 对象：

1. 通过 `MyAppHelperInterceptor.getRequestBodyMeta()` 获取请求元数据
2. 将 header 和元数据合并（元数据以 `meta-` 前缀添加到 header 列表）
3. 如果设置了 `requestBodyParser`，优先使用自定义解析器处理 body
4. **大 body (> mMaxBodyBytes)**：将 `Content-Type` 替换为 `application/json`，原始 `Content-Type` 保存为 `real-Content-Type`
5. **普通 body**：按 `mMaxBodyBytes` 截断读取

---

### 响应处理（统一 Tee 模式）

所有响应（JSON、SSE、大文件等）统一使用 Tee 模式处理，拦截器立即返回不阻塞。

#### `wrapResponseWithTee(response, identifier, isMock)`

使用 **Tee 机制** 包装响应体，核心思路：

1. 创建一个 `loggingBuffer` 用于记录流过的数据
2. 用 `ForwardingSource` 包装原始 source，在 `read()` 方法中通过 `sink.copyTo()` 将数据零拷贝写入 `loggingBuffer`（最多记录 1MB）
3. 当流结束（`bytesRead == -1`）时，上报完整的 `ResponseInfo` 到 Flipper，包含：
   - `X-Stream-Total-Bytes`：传输总字节数
   - `X-Stream-Logged-Bytes`：实际记录的字节数
4. body 为 null 时，直接上报响应头信息

> 拦截器不阻塞等待 body 读取完成，数据在调用方消费时自然流过并拷贝。

#### `isStreamingResponse(response)`（保留，不再被 intercept 直接调用）

检测响应是否为流式响应，满足以下任一条件返回 `true`：

| 条件 | 说明 |
|---|---|
| `Content-Type` 包含 `text/event-stream` | SSE (Server-Sent Events) |
| `Content-Type` 以 `image/`/`video/`/`audio/` 开头 | 媒体文件 |
| `Content-Type` 包含压缩格式 (`zip`/`rar`/`7z`/`gzip`/`tar`/`octet-stream`) | 压缩/二进制文件 |
| `Transfer-Encoding` 为 `chunked` | Chunked 编码传输 |
| `Content-Length` > 10MB | 大文件 |

#### `cloneBodyForResponse(response, maxBodyBytes)`（保留，仅异常处理路径使用）

从 response body source 中读取最多 `maxBodyBytes` 字节数据到 buffer 并返回克隆。

#### `convertResponse(response, bodyBuffer, identifier, isMock)`

将 OkHttp Response 转换为 `ResponseInfo`：设置 `requestId`、`timeStamp`、`statusCode`、`headers`、`isMock`、`body`。

---

### Header 转换

#### `convertHeader(headers, metaMap)`

将 OkHttp `Headers` 转换为 `List<NetworkReporter.Header>`。如果传入 `metaMap` 不为 null，会将其内容以 `meta-` 前缀追加到 header 列表。

---

### Mock 响应机制

支持从 Flipper 桌面端下发 mock 响应，流程如下：

```
Flipper 桌面端                         App 端
     │                                   │
     │  "mockResponses" 命令              │
     │  (包含 routes 数组)               │
     │ ──────────────────────────────────►│
     │                                   │  onConnect() 注册监听
     │                                   │  解析 routes → mMockResponseMap
     │                                   │
     │                                   │  intercept() 中检查
     │                                   │  getMockResponse(request)
     │                                   │  匹配 (url, method) → 返回 mock
     │                                   │
     │  断开连接                          │
     │ ──────────────────────────────────►│  onDisconnect() 清空 map
```

关键方法：
- `onConnect(connection)`：注册 `mockResponses` 接收器，解析桌面端下发的 mock 配置
- `getMockResponse(request)`：根据 URL + Method 查找 mock 响应
- `registerMockResponse()`：注册 mock 条目（已注册的不会覆盖）
- `convertFlipperObjectRouteToResponseInfo()`：将 Flipper 协议数据转换为 `ResponseInfo`

---

### 自定义请求体解析器

```java
FlipperOkhttpInterceptor.setRequestBodyParser(requestBodyParser);
```

适用场景：当应用拦截器对请求体进行了加密，需要在 Flipper 中展示解密后的内容。

接口定义：

```java
public interface RequestBodyParser {
    boolean parseRequestBoddy(Request request, Buffer bodyBuffer,
                              NetworkReporter.RequestInfo info, Map<String,String> bodyMetaData);
}
```

返回 `true` 表示已处理完毕，`convertRequest` 直接返回；返回 `false` 则继续默认逻辑。

---

### 与 NetworkFlipperPlugin 的交互

`FlipperOkhttpInterceptor` 通过 `mPlugin` 与 `NetworkFlipperPlugin` 交互：

- `mPlugin.reportRequest(RequestInfo)`：上报请求信息（发送 `newRequest` 到桌面端）
- `mPlugin.reportResponse(ResponseInfo)`：上报响应信息（body > 1MB 时自动分片发送 `partialResponse`，否则发送 `newResponse`）
- `mPlugin.setConnectionListener(this)`：注册 mock 响应连接监听

`NetworkFlipperPlugin.shouldStripResponseBody()` 会过滤掉 `event-stream`、`video/`、`application/zip` 类型的响应体（不发送到桌面端）。
