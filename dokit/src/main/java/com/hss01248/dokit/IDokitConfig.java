package com.hss01248.dokit;

import android.content.Context;

public interface IDokitConfig {

    void loadUrl(Context context, String url);

    void report(Object o);
}
