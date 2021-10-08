package com.sty.ne.skin_library;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.sty.ne.skin_library.model.SkinCache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 皮肤管理器
 * 加载应用资源（app内置：res/xxx）或者存储资源（下载皮肤包：xxx.skin）
 */
public class SkinManager {
    private static SkinManager instance;
    private Application application;
    private Resources appResources; //用于加载app内置资源（默认）
    private Resources skinResources; //用于加载皮肤包资源（皮肤包的）
    private String skinPackageName; //皮肤包资源所在包名（注：皮肤包不在app内，额不限包名）
    private boolean isDefaultSkin = true; //应用默认皮肤（app内置）
    private static final String ADD_ASSET_PATH = "addAssetPath"; //方法名
    private Map<String, SkinCache> cacheSkin;

    private SkinManager(Application application) {
        this.application = application;
        appResources = application.getResources();
        cacheSkin = new HashMap<>();
    }

    /**
     * 单例方法，目的是初始化app内置资源（越早越好，用户的操作可能是：换肤后的第二次冷启动）
     * @param application
     */
    public static void init(Application application){
        if(instance == null) {
            synchronized (SkinManager.class) {
                if(instance == null) {
                    instance = new SkinManager(application);
                }
            }
        }
    }

    public static SkinManager getInstance() {
        return instance;
    }

    public boolean isDefaultSkin() {
        return isDefaultSkin;
    }

    /**
     * 加载皮肤包的资源（颜色、图片）
     * @param skinPath 皮肤包路径，为空则加载app内置资源
     */
    public void loadSkinResources(String skinPath) {
        //优化：如果没有皮肤包或者没做换肤动作，方法不执行，直接返回
        if(TextUtils.isEmpty(skinPath)) {
            isDefaultSkin = true;
            return;
        }

        //优化：APP冷启动、热启动可以获取缓存对象
        if(cacheSkin.containsKey(skinPath)) {
            isDefaultSkin = false;
            SkinCache skinCache = cacheSkin.get(skinPath);
            if(null != skinCache) {
                skinResources = skinCache.getSkinResources();
                skinPackageName = skinCache.getSkinPackageName();
                return;
            }
        }

        try {
            //创建资源管理器（此处不能用：application.getAssets()）
            AssetManager assetManager = AssetManager.class.newInstance();
            //由于AssetManager中的addAssetPath和setApkAssets方法都被@hide，目前只能通过反射区执行方法
            Method addAssetPath = assetManager.getClass().getDeclaredMethod(ADD_ASSET_PATH, String.class);
            //设置私有方法可访问
            addAssetPath.setAccessible(true);
            //执行addAssetPath方法
            addAssetPath.invoke(assetManager, skinPath);
            //======================================================================================
            // 如果还是担心@hide限制，可以反射addAssetPathInternal()方法，参考源码366行 + 387行
            //======================================================================================

            //创建加载外部的皮肤包（sty.skin）文件中Resources（注：依然是本应用加载）
            skinResources = new Resources(assetManager, appResources.getDisplayMetrics(),
                    appResources.getConfiguration());

            //根据apk文件路径（皮肤包也是apk文件），获取该应用的包名，兼容5.0-9.0
            skinPackageName = application.getPackageManager()
                    .getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES).packageName;

            //无法获取皮肤包应用的包名，则加载APP内置资源
            isDefaultSkin = TextUtils.isEmpty(skinPackageName);
            if(!isDefaultSkin) { // 如果获取不到包名，证明是非法的皮肤包
                cacheSkin.put(skinPath, new SkinCache(skinResources, skinPackageName));
            }

            Log.d("sty", "skinPackageName -> " + skinPackageName);

        } catch (Exception e) {
            e.printStackTrace();
            //发生异常，预判：通过skinPath获取skinPackageName失败
            isDefaultSkin = true;
        }
    }

    /**
     * 参考：resources.arsc资源映射表
     * 通过id值获取资源 Name 和 Type
     * @param resourceId 资源ID值
     * @return 如果没有皮肤包则加载app内置资源ID，否则的话加载皮肤包指定资源ID
     */
    private int getSkinResourceIds(int resourceId) {
        //优化：如果没有皮肤包或者没有做换肤动作，直接返回app内置资源
        if(isDefaultSkin) {
            return resourceId;
        }

        //使用app内置资源加载，是因为内置资源与皮肤包资源--对应（"ne_bg", "drawable"）
        String resourceName = appResources.getResourceEntryName(resourceId);
        String resourceType = appResources.getResourceTypeName(resourceId);

        //动态获取皮肤包内的指定资源ID
        //getResources().getIdentifier("ne_bg", "drawable", "com.sty.ne.skin.packages");
        int skinResourceId = skinResources.getIdentifier(resourceName, resourceType, skinPackageName);

        //源码1924行：（0 is not a valid resource ID.）
        isDefaultSkin = skinResourceId == 0;
        return isDefaultSkin ? resourceId : skinResourceId;
    }

    //==============================================================================================

    public int getColor(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getColor(ids) : skinResources.getColor(ids);
    }

    public ColorStateList getColorStateList(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getColorStateList(ids) : skinResources.getColorStateList(ids);
    }

    //mipmap和drawable统一用法（待测）
    public Drawable getDrawableOrMipMap(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getDrawable(ids) : skinResources.getDrawable(ids);
    }

    public String getString(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getString(ids) : skinResources.getString(ids);
    }

    //返回值特殊情况：可能是color / drawable / mipmap
    public Object getBackgroundOrSrc(int resourceId) {
        //需要获取当前属性的类型名Resources.getResourceTypeName(resourceId)再判断
        String resourceTypeName = appResources.getResourceTypeName(resourceId);
        switch (resourceTypeName) {
            case "color":
                return getColor(resourceId);
            case "mipmap":
            case "drawable":
                return getDrawableOrMipMap(resourceId);
        }
        return null;
    }

    /**
     * 获取字体
     * @param resourceId
     * @return
     */
    public Typeface getTypeface(int resourceId) {
        //通过资源ID获取资源path，参考：resources.arsc资源映射表
        String skinTypefacePath = getString(resourceId);
        //路径为空，使用系统默认字体
        if(TextUtils.isEmpty(skinTypefacePath)) {
            return Typeface.DEFAULT;
        }
        return isDefaultSkin ? Typeface.createFromAsset(appResources.getAssets(), skinTypefacePath)
                : Typeface.createFromAsset(skinResources.getAssets(), skinTypefacePath);
    }

}
