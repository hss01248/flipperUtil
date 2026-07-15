# libc++_shared.so 依赖冲突扫描结果

- Variant: `debug`
- Configuration: `debugRuntimeClasspath`
- Scanned cache artifact count: 1131
- llvm-readelf: `/Users/hss/Library/Android/sdk/ndk/28.0.13004108/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-readelf`
- Note: 依赖列表来自 `debugRuntimeClasspath` 的 resolvedConfiguration；SO 扫描覆盖 modules-2 / transforms-2 / transforms-3，不受 pickFirst 影响

## ABI `arm64-v8a`

包含该 SO 的库数量：2

> **警告：** 存在重复，`pickFirst` 最终只会打入其中一个。

### `com.facebook.fbjni:fbjni:0.2.2`

- SO path: `/Users/hss/.gradle/caches/transforms-3/55bd0bd443ee4277a9ba62bdc2e6fcd5/transformed/jetified-fbjni-0.2.2/jni/arm64-v8a/libc++_shared.so`
- NDK: `r21b` (build 6352462, Android API 21)
- Compiler: `clang 9.0.8`
- Source: `exact (.note.android.ident)`

### `com.facebook.react:react-native:0.63.12`

- SO path: `/Users/hss/.gradle/caches/transforms-3/2b7f252a5b73a33d0ad6107701295e9a/transformed/jetified-react-native-0.63.12/jni/arm64-v8a/libc++_shared.so`
- NDK: `r27-beta1` (build 11883388, Android API 21)
- Compiler: `clang 18.0.1`
- Source: `exact (.note.android.ident)`

## ABI `armeabi-v7a`

包含该 SO 的库数量：2

> **警告：** 存在重复，`pickFirst` 最终只会打入其中一个。

### `com.facebook.fbjni:fbjni:0.2.2`

- SO path: `/Users/hss/.gradle/caches/transforms-3/55bd0bd443ee4277a9ba62bdc2e6fcd5/transformed/jetified-fbjni-0.2.2/jni/armeabi-v7a/libc++_shared.so`
- NDK: `r21b` (build 6352462, Android API 16)
- Compiler: `clang 9.0.8`
- Source: `exact (.note.android.ident)`

### `com.facebook.react:react-native:0.63.12`

- SO path: `/Users/hss/.gradle/caches/transforms-3/2b7f252a5b73a33d0ad6107701295e9a/transformed/jetified-react-native-0.63.12/jni/armeabi-v7a/libc++_shared.so`
- NDK: `r27-beta1` (build 11883388, Android API 21)
- Compiler: `clang 18.0.1`
- Source: `exact (.note.android.ident)`

## ABI `x86`

包含该 SO 的库数量：2

> **警告：** 存在重复，`pickFirst` 最终只会打入其中一个。

### `com.facebook.fbjni:fbjni:0.2.2`

- SO path: `/Users/hss/.gradle/caches/transforms-3/55bd0bd443ee4277a9ba62bdc2e6fcd5/transformed/jetified-fbjni-0.2.2/jni/x86/libc++_shared.so`
- NDK: `r21b` (build 6352462, Android API 16)
- Compiler: `clang 9.0.8`
- Source: `exact (.note.android.ident)`

### `com.facebook.react:react-native:0.63.12`

- SO path: `/Users/hss/.gradle/caches/transforms-3/2b7f252a5b73a33d0ad6107701295e9a/transformed/jetified-react-native-0.63.12/jni/x86/libc++_shared.so`
- NDK: `r27-beta1` (build 11883388, Android API 21)
- Compiler: `clang 18.0.1`
- Source: `exact (.note.android.ident)`

## ABI `x86_64`

包含该 SO 的库数量：2

> **警告：** 存在重复，`pickFirst` 最终只会打入其中一个。

### `com.facebook.fbjni:fbjni:0.2.2`

- SO path: `/Users/hss/.gradle/caches/transforms-3/55bd0bd443ee4277a9ba62bdc2e6fcd5/transformed/jetified-fbjni-0.2.2/jni/x86_64/libc++_shared.so`
- NDK: `r21b` (build 6352462, Android API 21)
- Compiler: `clang 9.0.8`
- Source: `exact (.note.android.ident)`

### `com.facebook.react:react-native:0.63.12`

- SO path: `/Users/hss/.gradle/caches/transforms-3/2b7f252a5b73a33d0ad6107701295e9a/transformed/jetified-react-native-0.63.12/jni/x86_64/libc++_shared.so`
- NDK: `r27-beta1` (build 11883388, Android API 21)
- Compiler: `clang 18.0.1`
- Source: `exact (.note.android.ident)`

