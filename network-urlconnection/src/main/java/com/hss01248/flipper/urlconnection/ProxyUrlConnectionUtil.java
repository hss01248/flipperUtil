package com.hss01248.flipper.urlconnection;

import com.blankj.utilcode.util.LogUtils;

import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;

/**
 * @Despciption
 * Android代理系统级HttpUrlConnection请求到第三方网络库-支持Http-2-0及HttpDNS :
 * https://fucknmb.com/2017/07/13/Android%E4%BB%A3%E7%90%86%E7%B3%BB%E7%BB%9F%E7%BA%A7HttpUrlConnection%E8%AF%B7%E6%B1%82%E5%88%B0%E7%AC%AC%E4%B8%89%E6%96%B9%E7%BD%91%E7%BB%9C%E5%BA%93-%E6%94%AF%E6%8C%81Http-2-0%E5%8F%8AHttpDNS/

 代理urlconnection: okhttp-urlconnection

 xx 用chrome的网络栈来代理,而不是代理webview的chrome网络栈: https://github.com/lizhangqu/cronet
如果要代理webview,则切入其shouldInterceptRequest方法,只代理http和https的请求

如果要本app设置网络代理,可以在application的oncreate设置:
Properties prop = System.getProperties();
prop.setProperty("proxySet", "true");
prop.setProperty("proxyHost", "218.241.131.227");
prop.setProperty("proxyPort", "6100");
原文链接：https://blog.csdn.net/fkq_2016/article/details/78329013
 * @Author hss
 * @Date 10/05/2022 11:05
 * @Version 1.0
 */
public class ProxyUrlConnectionUtil {

    /**
     * 需要时自行调用,默认不实现
     */
    public static void proxyUrlConnection(){
        try {
            //这种方式的OkHttp代理，切记不要使用任何拦截器，因为设置了也没有用，
            // 在OkHttpURLConnection.java中有个buildCall()方法，负责创建OkHttp的Call对象，在该方法中，会将我们设置进去的client的拦截器全部清空. 只能通过切面去加拦截器
            //todo 那么如何打标记?
            //通过这个来标识; clientBuilder.interceptors().clear();
            //    clientBuilder.interceptors().add(UnexpectedException.INTERCEPTOR);
            OkHttpClient client = new OkHttpClient.Builder()
                    //这里加的拦截器没有用,会被清空
                    .build();
            OkUrlFactory okUrlFactory = new OkUrlFactory(client);
            URL.setURLStreamHandlerFactory(okUrlFactory);
        } catch(Throwable e) {
            //ignore
            LogUtils.w(e);

        }
    }

    
}
