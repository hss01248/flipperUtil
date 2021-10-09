# flipperUtil

> flipper是Facebook推出的比之前的stetho更加强大的Android/ios debug工具,但是其内部插件都要写代码显式开启,
>
> 此处提供便捷的开启fipper的大部分常用的plugin的工具.
>
> 可直接使用远程脚本集成.
>
> 在remote3.gradle脚本上,集成dokit的功能
>
> 集成spiderman的显示崩溃信息的功能



[flipper官网](https://fbflipper.com/)



本工具类内部开启插件:

* network : 增强: 支持exif查看,自定义解密

* database  增强:支持外部数据库查看(sd卡其他目录的数据库)
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
或者
apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote3.gradle'//含滴滴的dokit
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



# 数据库

## 添加外部数据库:

示例:

初始化时,请求读存储权限(需要你自行处理).有权限后,将文件通过DBAspect.addDB(file)添加到列表,后续查看数据库时,就会显示你添加的外部数据库

```java
XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
        .request(new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                DBAspect.addDB(getFile("testxxxx.db"));
                DBAspect.addDB(getFile("imgdownload.db"));
            }
        });

 private File getFile(String name){
        String dbDir=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        dbDir += "/.yuv/databases";//数据库所在目录
        String dbPath = dbDir+"/"+name;//数据库路径
        File file = new File(dbPath);
        return file;
    }
```





# 一些注意事项:

## 仅debuggable=true时可用

flipper desktop端使用adb run-as 命令连接和操作手机app,非root时,此命令仅在debuggable=true的app上才能使用.

**机制上的限制,无法更改**

但是debuggable=true时,对于大型项目里,给测试打的测试包,运行明显比关闭debug开关时卡顿很多,影响测试体验.

## 网络抓包

可手动添加,可自动添加

### 自动添加的条件

* 如果你自己的项目里已经apply了aspectjx插件,那么在flipper_use_aspectjx=true的同时,请将下面依赖手动添加到你app项目的build.gradle里. 否则自动切入将不生效.

  [![](https://jitpack.io/v/hss01248/flipperUtil.svg)](https://jitpack.io/#hss01248/flipperUtil)

```groovy
debugImplementation com.github.hss01248.flipperUtil:flipper:1.1.1
```

* 因为aspectjx插件默认遍历所有lib,有严重编译性能问题,因此中大型项目要自动添加时,需要自行指定include的包内容,以加速编译过程. 具体参考remote2.gradle里aspectjx的配置部分

### 手动添加:

> 注意,是addNetWorkInterceptor

```java
FlipperUtil.addNetWorkInterceptor(OkHttpClient.Builder builder)
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



# 遇到的一些问题记录

## 如何在拦截器里拿到构建requestBody时的文件名

![image-20210702094550943](https://gitee.com/hss012489/picbed/raw/master/picgo/1625190354767-image-20210702094550943.jpg)



发现,在应用拦截器第一个,可以拿到,在网络拦截器里,大概率拿不到,body被上层应用拦截器和okhttp内部的网络拦截器重新构建了,丢失了原来的信息.

因此需要整两个拦截器相互配合:

添加一个到应用拦截器,index=0. 能拿到最原始的requestbody.然后把信息注入到header

再添加一个网络拦截器(flipperokhttpinterceptor),取出这些信息,发到flipper客户端.并移除之前注入的信息.



# windows上的flipper安装指南:

mac直接安装客户端即可,

windows有的电脑需要

安装watchman和openssl,并把其安装**目录/bin**加到系统环境变量中.

且把Android platform-tools下载,解压,把其**父目录**放到fipper的设置里



1 watchman

2 openssl  https://slproweb.com/products/Win32OpenSSL.html

3 Android sdk和ios sdk  https://developer.android.com/studio/releases/platform-tools?hl=zh-cn

## watchman的安装

![image-20210914173706800](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612226857-image-20210914173706800.jpg)

下载链接：

https://github.com/facebook/watchman/releases/tag/v2021.01.11.00

![image-20210914173749101](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612269137-image-20210914173749101.jpg)

下载完解压缩，放到英文目录

![image-20210914173811005](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612291043-image-20210914173811005.jpg)

然后将bin目录添加到环境变量：

右键点击我的电脑-》属性》高级系统设置》环境变量

![image-20210914173836459](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612316498-image-20210914173836459.jpg)

![image-20210914173910898](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612350936-image-20210914173910898.jpg)

## Android/ios sdk配置:

 https://developer.android.com/studio/releases/platform-tools?hl=zh-cn

![image-20210914174019916](https://gitee.com/hss012489/picbed/raw/master/picgo/1631612419954-image-20210914174019916.jpg)



# thanks

https://github.com/simplepeng/SpiderMan





# flipper的bug

long类型显示时会损失精度,但不影响实际网络请求

![image-20210916182903726](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788143768-image-20210916182903726.jpg)

flipper界面显示:

![image-20210916182119396](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788121743-image-20210916182119396.jpg)

chales抓包:



![image-20210916183147358](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788307400-image-20210916183147358.jpg)
