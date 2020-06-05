package com.sty.ne.skin_library.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.sty.ne.skin_library.R;
import com.sty.ne.skin_library.SkinManager;
import com.sty.ne.skin_library.core.ViewsMatch;
import com.sty.ne.skin_library.model.AttrsBean;

public class SkinnableLinearLayout extends LinearLayoutCompat implements ViewsMatch {

    private AttrsBean attrsBean;

    public SkinnableLinearLayout(Context context) {
        this(context, null);
    }

    public SkinnableLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinnableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        attrsBean = new AttrsBean();

        //根据自定义属性，匹配控件属性的类型集合，如：background + textColor
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkinnableLinearLayout,
                defStyleAttr, 0);
        //存储到临时JavaBean对象
        attrsBean.saveViewResource(typedArray, R.styleable.SkinnableLinearLayout);
        //这一句回收非常重要，obtainStyleAttributes()有语法提示
        typedArray.recycle();
    }

    @Override
    public void skinnableView() {
        //根据自定义属性，获取styleable中的background属性
        int key = R.styleable.SkinnableLinearLayout[R.styleable.SkinnableLinearLayout_android_background];
        //根据styleable获取控件某属性的resourceId
        int backgroundResourceId = attrsBean.getViewResource(key);
        if(backgroundResourceId > 0) {
            //是否默认皮肤
            if(SkinManager.getInstance().isDefaultSkin()) {
                //兼容包转换
                Drawable drawable = ContextCompat.getDrawable(getContext(), backgroundResourceId);
                //控件自带api，这里不用setBackgroundColor()的原因是在9.0测试不通过
                //setBackgroundDrawable本来过时了，但兼容包重写了该方法
                setBackground(drawable);
            }else {
                //获取皮肤包资源
                Object skinResourceId = SkinManager.getInstance().getBackgroundOrSrc(backgroundResourceId);
                //兼容包转换
                if(skinResourceId instanceof Integer) {
                    int color = (int) skinResourceId;
                    setBackgroundColor(color);
                    //setBackgroundResource(color); //未做兼容测试
                }else {
                    Drawable drawable = (Drawable) skinResourceId;
                    setBackground(drawable);
                }
            }
        }
    }
}
