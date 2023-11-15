package com.hss01248.dokit.parts;

import androidx.annotation.Nullable;

public interface IThirdPartyApk {

    String name();

    String pkgName();

    @Nullable String downloadUrl();
}
