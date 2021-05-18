# flipperUtil

> 提供便捷的开启fipper的全部plugin的工具.
>
> 直接使用远程脚本集成.



内部开启插件:

```
network,database,shareprefences,leakcanary,crashReporter,layout inspector
```



# 手动调用

```java
FlipperUtil.init(Application app, boolean enable,  ConfigCallback callback)
  
FlipperUtil.addInterceptor(OkHttpClient.Builder builder)
```

