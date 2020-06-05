package com.sty.ne.dynamicskinpeeler.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.sty.ne.dynamicskinpeeler.R;
import com.sty.ne.skin_library.SkinManager;
import com.sty.ne.skin_library.core.ViewsMatch;
import com.sty.ne.skin_library.model.AttrsBean;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 自定义控件
 */
public class CustomCircleView extends View implements ViewsMatch {
    private Paint mTextPain;
    private Paint mBgPain;
    private AttrsBean attrsBean;
    private String text;
    private int textSize;
    private int circleColorResId;
    private int circleTextColorResId;
    private Rect textBounds;

    public CustomCircleView(Context context) {
        this(context, null);
    }

    public CustomCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        attrsBean = new AttrsBean();

        //根据自定义属性，匹配控件属性的类型集合，如：circleColor
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomCircleView,
                defStyleAttr, 0);

        circleColorResId = typedArray.getResourceId(R.styleable.CustomCircleView_circleColor, 0);
        circleTextColorResId = typedArray.getResourceId(R.styleable.CustomCircleView_circleTextColor, 0);
        text = typedArray.getText(R.styleable.CustomCircleView_circleText).toString();
        textSize = typedArray.getInt(R.styleable.CustomCircleView_circleTextSize, 10);
        //存储到临时JavaBean对象
        attrsBean.saveViewResource(typedArray, R.styleable.CustomCircleView);
        // 这一句回收非常重要！obtainStyledAttributes()有语法提示！！
        typedArray.recycle();

        mTextPain = new Paint();
        mBgPain = new Paint();
        textBounds = new Rect();

        mBgPain.setColor(getResources().getColor(circleColorResId));
        //开启抗锯齿，平滑文字和圆弧的边缘
        mBgPain.setAntiAlias(true);

        //开启抗锯齿，平滑文字和圆弧的边缘
        mTextPain.setAntiAlias(true);
        //设置文本位于相对于原点的中间
        mTextPain.setTextAlign(Paint.Align.CENTER);
        mTextPain.setColor(getResources().getColor(circleTextColorResId));
        mTextPain.setTextSize(textSize);
        mTextPain.getTextBounds(text, 0, text.length(), textBounds);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取宽度一半
        int width = getWidth() / 2;
        //获取高度一半
        int height = getHeight() / 2;
        //设置半径为宽或者高的最小值（半径）
        int radius = Math.min(width, height);

        //利用canvas画一个圆
        canvas.drawCircle(width, height, radius, mBgPain);

        //画文字
        canvas.drawText(text, width,
                (getHeight() - textBounds.height()) / 2 + textBounds.height(), mTextPain);
    }

    @Override
    public void skinnableView() {
        //根据自定义属性，获取styleable中的circleColor属性
        int key = R.styleable.CustomCircleView[R.styleable.CustomCircleView_circleColor]; // = R.styleable.CustomCircleView_circleColor
        int resourceBgId = attrsBean.getViewResource(key);
        if(resourceBgId > 0) {
            if(SkinManager.getInstance().isDefaultSkin()) {
                int color = ContextCompat.getColor(getContext(), resourceBgId);
                mBgPain.setColor(color);
            }else {
                int color = SkinManager.getInstance().getColor(resourceBgId);
                mBgPain.setColor(color);
            }
        }

        key = R.styleable.CustomCircleView[R.styleable.CustomCircleView_circleTextColor]; // = R.styleable.CustomCircleView_circleColor
        int resourceTextColorId = attrsBean.getViewResource(key);
        if(resourceTextColorId > 0) {
            if(SkinManager.getInstance().isDefaultSkin()) {
                int color = ContextCompat.getColor(getContext(), resourceTextColorId);
                mTextPain.setColor(color);
            }else {
                int color = SkinManager.getInstance().getColor(resourceTextColorId);
                mTextPain.setColor(color);
            }
        }

        //根据自定义属性，获取styleable中的字体custom_typeface属性
        key = R.styleable.CustomCircleView[R.styleable.CustomCircleView_custom_typeface];
        int textTypefaceResourceId = attrsBean.getViewResource(key);
        if(textTypefaceResourceId > 0) {
            if(SkinManager.getInstance().isDefaultSkin()) {
                mTextPain.setTypeface(Typeface.DEFAULT);
            }else {
                mTextPain.setTypeface(SkinManager.getInstance().getTypeface(textTypefaceResourceId));
            }
        }

        invalidate();
    }
}
