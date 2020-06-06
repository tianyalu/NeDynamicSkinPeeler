# 基于皮肤包的动态换肤

[TOC]

## 一、实现效果图

![image](https://github.com/tianyalu/NeDynamicSkinPeeler/raw/master/show/show.gif)

## 二、皮肤包

皮肤包的本质还是`apk`，该apk拥有和主应用名称完全相同的配套资源文件，可以任意重命名该`apk`，包括后缀名。

### 2.1 为什么一定要使用`apk`作为皮肤包？

因为动态换肤底层依赖`AssetManager.cpp`，它要求必须有`AndroidManifest.xml`文件，所以只能使用`apk`。

### 2.2 皮肤包中重要的资源文件有哪些？

* `resources.arsc`资源映射表文件

* `AndroidManifest.xml`文件

* `res`目录下资源文件

* `assets`目录下资源文件  

> 皮肤包不关心`classes.dex`文件

## 三、实现思路浅析

### 3.1 总体思路

动态换肤与 [静态换肤](https://github.com/tianyalu/NeStaticSkinPeeler/blob/master/README.md) 总体思路大体相同，也是重写`onCreateView()`方法，在此方法中将布局文件中的系统控件替换为自定义控件，在自定义控件中实现换肤接口，在需要换肤的时候递归遍历`decorView`中实现了换肤接口的控件，调用其换肤方法实现换肤。

**根本区别**在于：动态换肤在执行换肤的时候，使用的是皮肤包（也即另一个`apk`）中的资源文件，而不是本项目中的另一套资源文件。所以本文的核心在于**如何通过反射拿到皮肤包的`AssetManager`，进而拿到`Resources`，然后根据拿到的皮肤包包名以及`Resources`真正获取皮肤包的资源**。

### 3.2 具体实现步骤

#### 3.2.1 自定义`SkinnableTextView`等控件
自定义`SkinnableTextView`等控件，此类控件继承原生控件，并实现了`ViewsMatch`接口。控件在初始化时，保存其自定义属性（主要为`background`和`textColor`等）和其对应的资源`id`到`AttrsBean`的`resourcesMap`中。换肤时，通过`ViewsMatch`接口的实现方法，经`AttrsBean`由`resourcesMap`中的资源`id`获取对应资源文件夹（主应用或皮肤包）下相应的资源：`background drawable`和`textColor`，然后设置给该自定义控件即可完成换肤。  

核心点在于以下两点：

* 通过反射拿到皮肤包的`AssetManager`，进而拿到`Resources`和皮肤包包名

  ```java
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
      if(!isDefaultSkin) {
        cacheSkin.put(skinPath, new SkinCache(skinResources, skinPackageName));
      }
    } catch (Exception e) {
      e.printStackTrace();
      //发生异常，预判：通过skinPath获取skinPackageName失败
      isDefaultSkin = true;
    }
  }
  ```

* 根据皮肤包包名以及`Resources`真正获取皮肤包的资源

  ```java
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
  ```
  
    

#### 3.2.2 实现`ActionBarUtils`等工具类
实现`ActionBarUtils`等工具类，该类中的方法获取主题颜色属性资源，即主应用或皮肤包`res/values`目录下`colors.xml`中颜色资源，设置给`ActionBar`，每次换肤时调用一次，即可完成换肤。  

#### 3.2.3 所有需要换肤的`Activity`都需继承`SkinActivity`  
该基类的核心在于三点：  

* `onCreate()`方法中提前设置工厂  

  ```java
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
      //我们要抢先一步，比系统还有早，拿到主动权
      LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), this);
      super.onCreate(savedInstanceState);
  }
  ```

* `onCreateView()`方法中将原生控件替换为自定义控件

  ```java
  //此函数 比系统的 onCreateView 函数更早执行，我们就能够采集布局里面所有的View
  //把xml中的系统控件替换成你自己的控件
  @Nullable
  @Override
  public View onCreateView(@Nullable View parent, @NonNull String name,
                           @NonNull Context context, @NonNull AttributeSet attrs) {
   if(openSkin()) {
      if(null == viewInflater) {
         viewInflater = new CustomAppCompatViewInflater(context);
      }
      viewInflater.setName(name); //TextView
      viewInflater.setAttrs(attrs); //TextView所有的属性集
      return viewInflater.autoMatch(); // 如果返回null,系统判断时 null, 就会走系统的，不影响
    }
    return super.onCreateView(parent, name, context, attrs); //继续正常走系统的
  }
  ```

* 换肤时，递归遍历`View`，对于实现了`ViewsMatch`接口的`View`，调用其接口方法，实现换肤操作

  ```java
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  protected void skinDynamic(String skinPath, int themeColorId) {
    //拿到资源 skinResource 皮肤包名
    SkinManager.getInstance().loadSkinResources(skinPath);
  
    //辅助的内容
    if(themeColorId != 0) {
      int themeColor = SkinManager.getInstance().getColor(themeColorId);
      StatusBarUtils.forStatusBar(this, themeColor);
      NavigationUtils.forNavigation(this, themeColor);
      ActionBarUtils.forActionBar(this, themeColor);
    }
  
    //真正开始换肤
    applyViews(getWindow().getDecorView());
  }
  
  /**
  * 控件回调监听，匹配上则给控件执行换肤方法
  * @param view
  */
  protected void applyViews(View view) {
    if(view instanceof ViewsMatch) {
      ViewsMatch viewsMatch = (ViewsMatch) view;
      viewsMatch.skinnableView();
    }
  
    if(view instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) view;
      int childCount = parent.getChildCount();
      for (int i = 0; i < childCount; i++) {
        applyViews(parent.getChildAt(i));
      }
    }
  }
  ```

#### 3.2.4 构建皮肤包

`skin_packages` `Module`是一个`Application`，构建该应用，生成apk文件，可以修改名称。

#### 3.2.5 获取皮肤包

将3.2.4生成的皮肤包上传到服务器，使用时从服务器下载，或者直接拷贝到手机存储中供应用加载。

## 四、原理分析

可参考：[静态换肤实现以及原理分析](https://github.com/tianyalu/NeStaticSkinPeeler)

