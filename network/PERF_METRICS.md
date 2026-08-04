# Flipper Network 性能指标说明

本文档说明 Flipper Network 插件在抓包时附加的 `perf-*` 与 `flipper-client-*` 字段的含义、计算方式，以及在使用 **OkHttp 3.12.x** 时的常见误读。

实现位置：

- 指标采集：`FlipperPerfEventListener`（OkHttp `EventListener`）
- 指标上报：`FlipperOkhttpInterceptor.appendPerfHeaders()`
- 客户端总耗时：`FlipperOkhttpInterceptor.addFlipperClientTimingHeadersToResponse()`

---

## 指标一览

| 字段 | 计算方式 | 含义 |
|------|----------|------|
| `flipper-client-elapsed-ms` | 拦截器内 `chain.proceed()` 返回时刻 − 拦截器入口时刻 | **Flipper 拦截器视角**的总耗时 |
| `perf-ttfb-ms` | `responseHeadersStart − callStart` | 从 Call 开始到**准备读响应头** |
| `perf-resp-header-ms` | `responseHeadersEnd − responseHeadersStart` | **阻塞读取响应头**的整段时间（见下文） |
| `perf-dns-ms` | `dnsEnd − dnsStart` | DNS 解析 |
| `perf-connect-ms` | `connectEnd − connectStart` | TCP 建连（**含 TLS 子阶段**） |
| `perf-tls-ms` | `secureConnectEnd − secureConnectStart` | TLS 握手（`connect-ms` 的子集） |
| `perf-req-header-ms` | `requestHeadersEnd − requestHeadersStart` | 写请求头 |
| `perf-req-body-ms` | `requestBodyEnd − requestBodyStart` | 写请求体 |

辅助字段：`perf-dns-host`、`perf-dns-addrs`、`perf-connect-peer-ip`、`perf-connect-peer-port`、`perf-tls-version`、`perf-tls-cipher`。

---

## 重要：OkHttp 3.12 下 `perf-resp-header-ms` 的语义

本项目依赖 **OkHttp 3.12.12**。在该版本中，`CallServerInterceptor` 的事件顺序为：

```text
responseHeadersStart()          // 准备读响应头之前
readResponseHeaders()           // 阻塞：等服务端 + 从 socket 读 status line 与 headers
responseHeadersEnd(response)    // 响应头读完之后
```

因此：

- **`perf-resp-header-ms` ≠「解析 HTTP header 花了多久」**
- **`perf-resp-header-ms` = 整段 `readResponseHeaders()` 的阻塞时间**，包括：
  1. 等待服务端返回数据（网络 RTT、服务端处理）
  2. 从 socket 读取 status line 与 header 行

OkHttp **4.3+** 才修正 `responseHeadersStart` 的触发时机（改为「响应头首包从服务端返回时」）。3.12 仍是在「准备读 header 之前」触发，导致该区间会吞掉大部分「等服务端」的时间。

### 典型误读

| 现象 | 实际原因 |
|------|----------|
| CDN 请求总耗时 ~100ms，但 `perf-resp-header-ms` ~90ms | 连接复用时无 DNS/TCP/TLS，网络等待几乎都落在这段 |
| `perf-resp-header-ms` 接近 `flipper-client-elapsed-ms` | 正常：Flipper 段内主要就是在等/读响应头 |
| API 冷连接 `perf-resp-header-ms` > 1s | 多半是等服务端首包 + 读 header，不是 header 解析慢 |

---

## 与 `flipper-client-elapsed-ms` 的差异

两者**起点不同**，不能直接相加对比：

```text
callStart                          ← perf-ttfb 起点
  │
  ├─ 其它 Application Interceptor（Flipper 之前的拦截器）
  │
  ▼
Flipper 拦截器入口                  ← flipper-client-elapsed 起点
  │
  ├─ clone body、reportRequest 等
  ├─ chain.proceed() → 网络层
  │     ├─ responseHeadersStart
  │     ├─ readResponseHeaders()   ← perf-resp-header-ms
  │     └─ responseHeadersEnd
  │
  ▼
chain.proceed() 返回               ← flipper-client-elapsed 终点
```

示例（CDN 图片，连接复用）：

| 指标 | 值 |
|------|-----|
| `flipper-client-elapsed-ms` | 103 |
| `perf-ttfb-ms` | 95 |
| `perf-resp-header-ms` | 94 |

解读：

- `ttfb(95) − elapsed(103)` 的差 ≈ `callStart` 到 Flipper 入口之间其它拦截器的耗时（不在 `elapsed` 里）
- Flipper 可见段：`103 ≈ (95 − 前置拦截器耗时) + 94`，其中 **94ms 是等/读响应头**

---

## 冷连接 API 请求拆解示例

`ec-api.akulaku.com` 一次冷连接：

| 字段 | 值 | 说明 |
|------|-----|------|
| `perf-dns-ms` | 131 | DNS |
| `perf-connect-ms` | 962 | TCP + TLS（含下面 TLS 695ms） |
| `perf-tls-ms` | 695 | TLS 握手 |
| `perf-ttfb-ms` | 1129 | ≈ DNS + Connect |
| `perf-resp-header-ms` | 1224 | 等首包 + 读 header（非纯解析） |
| `perf-req-header-ms` / `perf-req-body-ms` | 0 | 发包几乎无耗时 |

瓶颈主要在 **建连（尤其 TLS）**；`perf-resp-header-ms` 大是因为 OkHttp 3.12 把「等服务端」也算进该区间，不代表 header 解析本身慢。

---

## 如何正确看耗时

1. **看总耗时**：优先 `flipper-client-elapsed-ms`（Flipper 拦截器视角）。
2. **看建连**：`perf-dns-ms`、`perf-connect-ms`、`perf-tls-ms`（无这些字段时多为连接复用）。
3. **看服务端/网络等待**：在 OkHttp 3.12 下，**不要单独把 `perf-resp-header-ms` 当成「读 header」**；它更接近「发完请求后，到响应头读完」的等待。
4. **近似服务端等待**（未单独上报时可心算）：  
   `perf-resp-header-ms` 在 3.12 下已包含大部分服务端等待；更细拆分需升级 OkHttp 或增加 `requestHeadersEnd → responseHeadersStart` 等自定义指标。

---

## 已知限制与后续改进方向

| 项 | 说明 |
|----|------|
| 时间源 | 使用 `System.currentTimeMillis()`，毫秒精度 |
| OkHttp 版本 | 3.12.x 事件语义与 4.3+ 不同，见上文 |
| timings 回退匹配 | `appendPerfHeaders` 在 map 未命中时会按 URL 迭代匹配，并发同 URL 可能误配（见 `perf-debug-found-by: iteration-match`） |
| 建议改进 | 重命名 `perf-resp-header-ms`、增加 `perf-server-wait-ms`、改用 `nanoTime`、升级 OkHttp 4.3+ |

---

## 调试字段

| 字段 | 含义 |
|------|------|
| `perf-debug-call-hash` | Call 对象 identityHashCode |
| `perf-debug-map-size` | `CALL_TIMINGS_MAP` 大小 |
| `perf-debug-found-by` | 若为 `iteration-match`，表示 timings 来自 URL 回退匹配 |
| `perf-error` | 未找到 timings 或其它错误 |

---

## 参考

- OkHttp 3.12 `CallServerInterceptor`：`responseHeadersStart` 在 `readResponseHeaders()` **之前**触发
- OkHttp 4.3+ 变更：`responseHeadersStart` 改为「响应头首包从服务端返回时」
- 项目 OkHttp 版本：`network/build.gradle` → `okhttp:3.12.12`
