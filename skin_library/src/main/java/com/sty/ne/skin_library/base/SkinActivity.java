package com.sty.ne.skin_library.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sty.ne.skin_library.SkinManager;
import com.sty.ne.skin_library.core.CustomAppCompatViewInflater;
import com.sty.ne.skin_library.core.ViewsMatch;
import com.sty.ne.skin_library.utils.ActionBarUtils;
import com.sty.ne.skin_library.utils.NavigationUtils;
import com.sty.ne.skin_library.utils.StatusBarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

/**
 * 换肤Activity基类
 *
 * 用法：
 * 1、继承此类
 * 2、重新openChangeSkin()方法
 *
 * 原理详情可参考静态换肤：https://github.com/tianyalu/NeStaticSkinPeeler
 */
public class SkinActivity extends AppCompatActivity {
    private CustomAppCompatViewInflater viewInflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        if(openChangeSkin()) {
            if(viewInflater == null) {
                viewInflater = new CustomAppCompatViewInflater(context);
            }
            viewInflater.setName(name);
            viewInflater.setAttrs(attrs);
            return viewInflater.autoMatch();
        }

        return super.onCreateView(parent, name, context, attrs);
    }

    /**
     * 是否开启换肤，增加此开关是为了避免开发者误继承此父类，导致未知bug
     * @return
     */
    protected boolean openChangeSkin() {
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void defaultSkin(int themeColorId) {
        this.skinDynamic(null, themeColorId);
    }

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
}
