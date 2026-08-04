---
name: upgrade-to-gradle8
description: >-
  Upgrade Android/Gradle projects to Gradle 8 (wrapper 8.11.1-all) and AGP 8.9.1.
  Covers namespace, buildConfig, buildToolsVersion removal, SDK 36/minSdk 21,
  nonTransitiveRClass, and gradle8-compact.gradle compatibility.
  Use when upgrading to Gradle 8, AGP 8, migrating from Gradle 7, fixing AGP 8
  build errors (namespace missing, R cannot find symbol, buildConfigField),
  or when the user mentions Gradle8 / AGP8 升级.
---

# 升级到 Gradle 8

## 版本锁定（必须）

1. **Gradle**：`gradle-8.11.1-all`  
   - `gradle/wrapper/gradle-wrapper.properties` →  
     `distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-all.zip`
2. **AGP（gradle tools）**：`8.9.1`  
   - 根 `build.gradle` → `classpath "com.android.tools.build:gradle:8.9.1"`

> 不得擅自更换上述版本；若需调整，先与用户确认。

## 兼容脚本

AGP8 相关兼容逻辑集中在 `gradle8-compact.gradle`，根 `build.gradle` 中：

```gradle
apply from: 'gradle8-compact.gradle'
```

后续兼容开关优先追加到该脚本，不要分散改各 module。

## Namespace（AGP 8.0+ 强制）

- 所有 `com.android.application` / `com.android.library` module 必须在 `build.gradle` 声明 `namespace`。
- 取值优先用原 `AndroidManifest.xml` 的 `package`；迁移后可移除 Manifest 的 `package`。
- 纯 JVM module 不需要。

```gradle
android {
    namespace 'com.example.mymodule'
}
```

## buildFeatures.buildConfig（AGP 8.0+ 默认关闭）

- 若使用 `buildConfigField`，需开启 `buildFeatures.buildConfig true`。
- 本仓库通过 `gradle8-compact.gradle` 对所有 Android module 统一开启，不要逐个改 module。

## 移除 buildToolsVersion

- AGP 8.9.1 自带默认 Build Tools（最低 35.0.0），不要写 `buildToolsVersion`。
- 若写了低于最低要求的版本会 warning 并被忽略；处理方式是**删除该行**，不要改成 35.0.0 去压警告。

## SDK 版本约定

所有 Android module 统一：

| 配置 | 值 |
|------|----|
| `compileSdkVersion` | 36 |
| `targetSdkVersion` | 36 |
| `minSdkVersion` | 21 |

```gradle
android {
    compileSdkVersion 36
    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 36
    }
}
```

- 新增 module 直接使用上表，不要写旧版本（如 32/33、15/19）。
- 不要 apply 会强制写回旧 SDK 的历史脚本（如 `migrate_to_sdk32.gradle`）。

## nonTransitiveRClass（AGP 8.0+ 默认 true）

- **现象**：Gradle7 能编过，AGP8 报 `cannot find symbol: variable id / location: class R`（如 `R.id.ll_container`）。
- **原因**：AGP 8 默认 `android.nonTransitiveRClass=true`，每个 module 的 `R` **只含本 module 资源**，不再合并依赖库资源。代码若用本 module 的 `R` 访问依赖里的 id/layout，就会找不到。
- **处理方式（本仓库约定）**：在 `gradle.properties` 关闭传递限制，恢复 Gradle7 行为：

```properties
android.nonTransitiveRClass=false
```

- **长期更好的改法**（可选）：引用依赖库自己的 `R`，例如 DialogUtil 资源用 `com.hss01248.dialog.R.id.xxx`，而不是本 module 的 `R`。
