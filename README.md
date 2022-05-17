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

# 所有功能列表

### from flipper: pc上客户端查看app各种数据信息的工具

* network : 增强: 支持exif查看,可自定义请求体解密,可查看urlconnection

* database  支持外部数据库查看(sd卡其他目录的数据库)
* shareprefences
* crashReporter
* layout inspector
* mmkv查看工具

### from dokit: 手机上查看app各种数据信息的工具

* 慢函数定位: 对dokit自带工具的增强. 可logcat打印,可自定义上报主线程卡顿
* 数据库查看: 郭霖的glance. 只能查看内部数据库
* activity+fragment实时变化可视化工具
* 一键跳转app详情页
* app信息查看: dokit自带.
* 平时积累的一些第三方开发工具-以外部apk形式存在,点击跳转应用商店.
* 日志: 手机上的logcat. dokit自带,灵敏度高,无卡顿
* 沙盒浏览; dokit自带. 比较不错可以查看内部外部应用的私有目录
* h5任意门: dokit自带. 有扫码跳转功能,以及浏览历史功能
* 自动分析第三方库信息: dokit自带
* 崩溃后弹出activity显示崩溃栈: spiderman提供
* 性能三大件: 帧率,cpu,内存: dokit提供,界面显示实时动态曲线.   todo 增加与界面关联的上报功能
* ui工具: 层级,边框,控件属性查看,取色器. dokit提供.    todo 集成uetool. 可实时更改属性调ui

### 直接通过远程配置使用:

在根目录的build.gradle的buildsricpt块里如下引用远程脚本:

```groovy
buildscript {
  apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote3.gradle'
}
```

中国国内可使用cdn:

```groovy
apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote3.gradle'
```

### 如果项目里有react native时的包冲突问题

如果遇到com.facebook.fbjni:fbjni和com.facebook.fbjni:fbjni-java-only包冲突,那么

可以在你的项目里使用:

```groovy
 project.configurations{
            all*.exclude group: 'com.facebook.fbjni', module: 'fbjni'
        }
```



远程脚本配置原理可参考:

[gradle远程脚本](https://github.com/hss01248/notebook2/blob/master/%E5%B7%A5%E7%A8%8B%E6%96%B9%E6%B3%95-%E7%BC%96%E8%AF%91%E5%92%8C%E8%87%AA%E5%8A%A8%E5%8C%96/gradle%E8%BF%9C%E7%A8%8B%E8%84%9A%E6%9C%AC.md)

## 初始化

>  不需要,库内部已自动初始化.  
>
> 网络抓包功能已通过aop手段(aspectjx)直接加到okhttpclient的构造中,无需再添加拦截器



# 卡顿-慢函数检测和统计

## 效果

### 日志打印

![image-20211111101848225](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1636597133931-image-20211111101848225.jpg)



### 上报到bugly

![image-20211111103645491](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1636598205560-image-20211111103645491.jpg)

上报的实现

```java
MyDokit.setConfig(new IDokitConfig() {
    @Override
    public void loadUrl(Context context, String url) {
       
    }

    @Override
    public void report(Object o) {
        if(o instanceof Throwable){
          //上报到你自己项目的crash/exception统计平台,比如bugly, firebase,sentry
            XReporter.reportException((Throwable) o);
        }
    }
});
```

### 指定对哪些包路径插桩

默认对包名往前移位后插桩

比如包名为com.hss01248.imagedemo, 那么就对类路径带com.hss01248的进行插桩

#### 具体项目推荐自己配置:

拷贝下方插件配置(从project.ext开始拷贝):

修改其中的packageNames内容

```groovy
            //dokit插件
            if(!isRelease()){
                if(hasApplyDokit(project)){
                    println("已经应用了dokit插件,不再重复使用. 可以参考此脚本配置慢函数打印上报,阈值80ms. 超过阈值的,且运行在主线程的,被认为是慢函数")
                    return
                }
                println("你的项目没有应用dokit插件,使用默认配置. 推荐拷贝此处脚本,自行配置插桩的包名: packageNames")
                println("应用包名往前一个:"+getPkgName(project)+" , 默认使用此作为插桩包名")
                project.ext{
                    DOKIT_PLUGIN_SWITCH=true
                    DOKIT_METHOD_SWITCH=true
                    DOKIT_LOG_SWITCH=true
                    DOKIT_METHOD_STRATEGY=1
/*# dokit全局配置
# 插件开关
DOKIT_PLUGIN_SWITCH=true
# DOKIT读取三方库会和booster冲突 如果你的项目中也集成了booster 建议将开关改成false
DOKIT_THIRD_LIB_SWITCH=true
# 插件日志
DOKIT_LOG_SWITCH=true
# 自定义Webview的全限定名 主要是作用于h5 js抓包和数据mock
DOKIT_WEBVIEW_CLASS_NAME=com/didichuxing/doraemonkit/widget/webview/MyWebView
# dokit 慢函数开关
DOKIT_METHOD_SWITCH=true
# dokit 函数调用栈层级
DOKIT_METHOD_STACK_LEVEL=2
# 0:默认模式 打印函数调用栈 需添加指定入口  默认为application onCreate 和attachBaseContext
# 1:普通模式 运行时打印某个函数的耗时 全局业务代码函数插入
DOKIT_METHOD_STRATEGY=1*/
                }

                project.apply plugin: 'com.didi.dokit'

                project.dokitExt {
                    //通用设置
                    comm {
                        //地图经纬度开关
                        gpsSwitch false
                        //网络开关
                        networkSwitch false
                        //大图开关
                        bigImgSwitch false
                        //webView js 抓包
                        webViewSwitch false
                    }

                    slowMethod {
                        //普通模式配置 对应gradle.properties中DOKIT_METHOD_STRATEGY=1
                        normalMethod {
                            //默认值为 80ms 小于该值的函数在运行时不会在控制台中被打印
                            thresholdTime 80
                            //需要针对函数插装的包名 千万不要用我默认的配置 如果有特殊需求修改成项目中自己的项目包名 假如不需要可以去掉该字段
                            //getSlowMethodAsmPkgNames(project) 不能动态,只能静态配置
                            packageNames = ["com.facebook",getPkgName(project)]
                            //getPkgName(project)
                            //不需要针对函数插装的包名&类名 千万不要用我默认的配置 如果有特殊需求修改成项目中自己的项目包名 假如不需要可以去掉该字段
                            methodBlacklist = ["com.didichuxing.doraemondemo.dokit"]
                        }
                    }
                }
                println("插桩包名:" + project.dokitExt.slowMethod.normalMethod.packageNames)
            }
```

 



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
debugImplementation com.github.hss01248.flipperUtil:flipper:1.4.6
```

* 因为aspectjx插件默认遍历所有lib,有严重编译性能问题,因此中大型项目要自动添加时,需要自行指定include的包内容,以加速编译过程. 具体参考remote3.gradle里aspectjx的配置部分

### 手动添加:

> 注意,是addNetWorkInterceptor

```java
FlipperUtil.addNetWorkInterceptor(OkHttpClient.Builder builder)
  //或者:
FlipperUtil.getInterceptor()//可能为null

```

## 抓包urlconnection

内置功能,默认关闭.

需要手动开启:

```java
implementation 'com.github.hss01248.flipperUtil:network-urlconnection:1.4.6'
```

在network界面,requestHeader里有flipper-fromUrlConnection=1来标识





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



# eventbus观测

> 主要参考https://github.com/SaqibJDev/EventBusLogger-Flipper, 使用aop切入eventbus的post方法和removeSticky方法.

![image-20211223165619703](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1640249785394-image-20211223165619703.jpg)



# 依赖打印和task tree输出



```groovy
buildscript {
  apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/deps/depsLastestChecker.gradle'
}
```





# thanks

https://github.com/simplepeng/SpiderMan

https://github.com/SaqibJDev/EventBusLogger-Flipper





# flipper的bug

long类型显示时会损失精度,但不影响实际网络请求

![image-20210916182903726](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788143768-image-20210916182903726.jpg)

flipper界面显示:

![image-20210916182119396](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788121743-image-20210916182119396.jpg)

chales抓包:



![image-20210916183147358](https://cdn.jsdelivr.net/gh/hss01248/picbed@master/pic/1631788307400-image-20210916183147358.jpg)0
