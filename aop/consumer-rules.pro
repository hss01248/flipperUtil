-keep class com.hss01248.flipper.aop.** { *; }

-keep class org.aspectj.** { *;}
-keep @org.aspectj.lang.annotation.Aspect class * {*;}

-keepclassmembers class * {
    @org.aspectj.lang.annotation.Around <methods>;
}
-keepclassmembers class * {
    @org.aspectj.lang.annotation.Before <methods>;
}
-keepclassmembers class * {
    @org.aspectj.lang.annotation.Pointcut <methods>;
}
-keep class org.greenrobot.eventbus.EventBus.** { *;}
-keep class androidx.lifecycle.LiveData.** { *;}
-keep class androidx.lifecycle.** { *;}
-keep class io.reactivex.** { *;}

