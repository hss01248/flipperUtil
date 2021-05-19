# flipperUtil

> flipper是Facebook推出的比之前的stetho更加强大的Android/ios debug工具,但是其内部插件都要写代码显式开启,
>
> 此处提供便捷的开启fipper的大部分常用的plugin的工具.
>
> 可直接使用远程脚本集成.



[flipper官网](https://fbflipper.com/)



本工具类内部开启插件:

```
network,database,shareprefences,leakcanary,crashReporter,layout inspector
```



# 手动调用

## gradle

```groovy

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}


app.gradle 里:
  
debugImplementation "com.github.hss01248.flipperUtil:flipper:1.0.2"
releaseImplementation "com.github.hss01248.flipperUtil:flipper-no-op:1.0.2"
```



### 也可使用远程配置:

在app.gradle里:

```groovy
apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote.gradle'

中国国内可使用cdn:
  apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote.gradle'
```



## 初始化

```java
库内部已自动初始化.

okhttp构建client时添加:  
FlipperUtil.addInterceptor(OkHttpClient.Builder builder)
  或者
FlipperUtil.getInterceptor()
  
  
如果使用sandbox功能来动态化一些配置,可以使用:
addConfigBox(Context context,ConfigCallback callback)
  
```





# 注意:

```
内部使用的okhttp是4.9.0, 如果需要使用3.12.x(兼容Android4.4),请强制指定版本(dependces同级)
 configurations {
     all {
         resolutionStrategy {
             force "com.squareup.okhttp3:okhttp:3.12.1"
         }
     }
 }
```

### 也可以直接使用远程配置:

```groovy
apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote_forceokhttp3_12.gradle'

中国国内可使用cdn:
  apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote_forceokhttp3_12.gradle'
```

