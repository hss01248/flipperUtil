# flipperUtil

> flipper是Facebook推出的比之前的stetho更加强大的Android/ios debug工具,但是其内部插件都要写代码显式开启,
>
> 此处提供便捷的开启fipper的大部分常用的plugin的工具.
>
> 可直接使用远程脚本集成.



[flipper官网](https://fbflipper.com/)



本工具类内部开启插件:

* network

* database
* shareprefences
* crashReporter
* layout inspector
* mmkv  自动初始化,自行衡量是否影响项目



### 直接通过远程配置使用:

在根目录的build.gradle的buildsricpt块里如下引用远程脚本:

```groovy
buildscript {
  apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote2.gradle'
  //或者使用某个特定commit(请自行查看最新commit,不要直接拷贝下面的):
  apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/68c7ca679282f8fc4aa6e3a8c01fa1290fd00d19/remote2.gradle'
  //68c7ca679282f8fc4aa6e3a8c01fa1290fd00d19 某次commit的sha1

}
```

中国国内可使用cdn:

```groovy
apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote2.gradle'
```



### 如果项目里有RN,使用:

```groovy
apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote2_forRN.gradle'
```







远程脚本配置可参考:

[gradle远程脚本](https://github.com/hss01248/notebook2/blob/master/%E5%B7%A5%E7%A8%8B%E6%96%B9%E6%B3%95-%E7%BC%96%E8%AF%91%E5%92%8C%E8%87%AA%E5%8A%A8%E5%8C%96/gradle%E8%BF%9C%E7%A8%8B%E8%84%9A%E6%9C%AC.md)

## 初始化

>  不需要,库内部已自动初始化.  
>
> 网络抓包功能已通过aop手段(aspectjx)直接加到okhttpclient的构造中,无需再添加拦截器



# 一些注意事项:

## 仅debuggable=true时可用

flipper desktop端使用adb run-as 命令连接和操作手机app,非root时,此命令仅在debuggable=true的app上才能使用.

**机制上的限制,无法更改**

但是debuggable=true时,对于大型项目里,给测试打的测试包,运行明显比关闭debug开关时卡顿很多,影响测试体验.

## 网络抓包

可手动添加,可自动添加

### 自动添加的条件

* 在gradle.properties文件里配置了flipper_use_aspectjx=true时,将自动apply aspectjx插件,切入okhttpclient.Builder.build()方法.

* 如果你自己的项目里已经apply了aspectjx插件,那么在flipper_use_aspectjx=true的同时,请将下面依赖手动添加到你app项目的build.gradle里. 否则自动切入将不生效.

```groovy
debugImplementation com.github.hss01248.flipperUtil:flipper:1.0.9
```

* 因为aspectjx插件默认遍历所有lib,有严重编译性能问题,因此中大型项目要自动添加时,需要自行指定include的包内容,以加速编译过程. 具体参考remote2.gradle里aspectjx的配置部分

### 手动添加:

```java
FlipperUtil.addInterceptor(OkHttpClient.Builder builder)
  //或者:
FlipperUtil.getInterceptor()//可能为null

```



## 项目里有react native时的包冲突问题

如果遇到com.facebook.fbjni:fbjni和com.facebook.fbjni:fbjni-java-only包冲突,那么

可以在你的项目里使用:

```groovy
 project.configurations{
            all*.exclude group: 'com.facebook.fbjni', module: 'fbjni'
        }
```

