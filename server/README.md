# SSE demo server

JDK-only Server-Sent Events endpoint for the FlipperDemo Android app. Built with **Maven**（不依赖 Gradle，可与本机其它 Gradle/Android 工程并行，避免争用守护进程与文件锁）。

## 环境

- JDK 11+（与 `pom.xml` 中 `maven.compiler.release` 一致）
- [Apache Maven](https://maven.apache.org/install.html) 3.6+

## Run

```bash
chmod +x run.sh
./run.sh
```

指定端口（默认 `18080`）：

```bash
./run.sh 19090
```

或直接使用 Maven / Java：

```bash
mvn -q compile
java -cp target/classes com.flipperdemo.sse.SseHttpServer
java -cp target/classes com.flipperdemo.sse.SseHttpServer 19090
```

打包为可执行 jar（`Main-Class` 已写入 manifest）：

```bash
mvn -q package
java -jar target/sse-demo-server-1.0.0-SNAPSHOT.jar
java -jar target/sse-demo-server-1.0.0-SNAPSHOT.jar 19090
```

## Endpoints

- `GET /` — short help text
- `GET /sse` — SSE stream (`event:` + `data:` JSON), ~60 seconds then closes

## Android

- **Emulator**: use `http://10.0.2.2:18080/sse` (maps to host loopback).
- **Physical device**: use your computer’s LAN IP, e.g. `http://192.168.1.5:18080/sse`, same Wi‑Fi as the phone.

## Spring Boot（可选）

若更习惯 Spring Boot，可新建独立 Maven 工程，依赖 `spring-boot-starter-web`，用 `SseEmitter` 写 `event:` / `data:`；端口与路径保持 `18080`、`/sse`，与 `MainActivity` 里的 `SSE_URL` 一致即可。
