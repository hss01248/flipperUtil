# flipperUtil

> flipper是Facebook推出的比之前的stetho更加强大的Android/ios debug工具,但是其内部插件都要写代码显式开启,
>
> 此处提供便捷的开启fipper的大部分常用的plugin的工具.
>
> 可直接使用远程脚本集成.



[flipper官网](https://fbflipper.com/)



本工具类内部开启插件:

```
network,database,shareprefences,crashReporter,layout inspector
```



### 直接通过远程配置使用:

在根目录的build.gradle的buildsricpt块里如下引用远程脚本:

```groovy
buildscript {
  apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/remote2.gradle'
  //或者使用某个特定commit:
  apply from 'https://raw.githubusercontent.com/hss01248/flipperUtil/27756184e1b8c5aeb305bd2943473c4aaafa0f8a/remote2.gradle'

}
```

中国国内可使用cdn:

```groovy
apply from: 'https://cdn.jsdelivr.net/gh/hss01248/flipperUtil@master/remote2.gradle'
```



远程脚本配置可参考:

[gradle远程脚本](https://github.com/hss01248/notebook2/blob/master/%E5%B7%A5%E7%A8%8B%E6%96%B9%E6%B3%95-%E7%BC%96%E8%AF%91%E5%92%8C%E8%87%AA%E5%8A%A8%E5%8C%96/gradle%E8%BF%9C%E7%A8%8B%E8%84%9A%E6%9C%AC.md)

## 初始化

>  不需要,库内部已自动初始化.  
>
> 网络抓包功能已通过aop手段(aspectjx)直接加到okhttpclient的构造中,无需再添加拦截器




