package com.sty.ne.skin_library.model;

//皮肤包 == 资源描述对象

import android.content.res.Resources;

//AndroidManifest.xml 皮肤包的包名
public class SkinCache {

    private Resources skinResources; //用于加载皮肤包资源
    private String skinPackageName; //皮肤包资源所在包名（注：皮肤包不在app内，也不限包名）

    public SkinCache(Resources skinResources, String skinPackageName) {
        this.skinResources = skinResources;
        this.skinPackageName = skinPackageName;
    }

    public Resources getSkinResources() {
        return skinResources;
    }

    public String getSkinPackageName() {
        return skinPackageName;
    }
}
