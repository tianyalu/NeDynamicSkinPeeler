package com.sty.ne.skin_library.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.sty.ne.skin_library.R;
import com.sty.ne.skin_library.SkinManager;
import com.sty.ne.skin_library.core.ViewsMatch;
import com.sty.ne.skin_library.model.AttrsBean;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

public class SkinnableButton extends AppCompatButton implements ViewsMatch {
    private AttrsBean attrsBean;

    public SkinnableButton(Context context) {
        this(context, null);
    }

    public SkinnableButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public SkinnableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        attrsBean = new AttrsBean();

        //根据自定义属性，匹配控件属性的类型集合，如：background + textColor
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkinnableButton,
                defStyleAttr, 0);
        //存储到临时JavaBean对象
        attrsBean.saveViewResource(typedArray, R.styleable.SkinnableButton);
        //这一句回收非常重要，obtainStyleAttributes()有语法提示
        typedArray.recycle();
    }

    @Override
    public void skinnableView() {
        //根据自定义属性，获取styleable中的background属性
        int key = R.styleable.SkinnableButton[R.styleable.SkinnableButton_android_background];
        //根据styleable获取控件某属性的resourceId
        int backgroundResourceId = attrsBean.getViewResource(key);
        if(backgroundResourceId > 0) {
            //是否默认皮肤
            if(SkinManager.getInstance().isDefaultSkin()) {
                //兼容包转换
                Drawable drawable = ContextCompat.getDrawable(getContext(), backgroundResourceId);
                //控件自带api，这里不用setBackgroundColor()的原因是在9.0测试不通过
                //setBackgroundDrawable本来过时了，但兼容包重写了该方法
                setBackgroundDrawable(drawable);
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
                    setBackgroundDrawable(drawable);
                }
            }
        }

        // 根据自定义属性，获取styleable中的textColor属性
        key = R.styleable.SkinnableButton[R.styleable.SkinnableButton_android_textColor];
        int textColorResourceId = attrsBean.getViewResource(key);
        if(textColorResourceId > 0) {
            if(SkinManager.getInstance().isDefaultSkin()) {
                ColorStateList color = ContextCompat.getColorStateList(getContext(), textColorResourceId);
                setTextColor(color);
            }else {
                ColorStateList color = SkinManager.getInstance().getColorStateList(textColorResourceId);
                setTextColor(color);
            }
        }

        //根据自定义属性，获取styleable中的字体custom_typeface属性
        key = R.styleable.SkinnableButton[R.styleable.SkinnableButton_custom_typeface];
        int textTypefaceResourceId = attrsBean.getViewResource(key);
        if(textTypefaceResourceId > 0) {
            if(SkinManager.getInstance().isDefaultSkin()) {
                setTypeface(Typeface.DEFAULT); // 证明是空的，DEFAULT==恢复默认
            }else { // 加载皮肤包的
                setTypeface(SkinManager.getInstance().getTypeface(textTypefaceResourceId));
            }
        }
    }
}
