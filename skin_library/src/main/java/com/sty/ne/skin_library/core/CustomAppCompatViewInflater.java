package com.sty.ne.skin_library.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.sty.ne.skin_library.views.SkinnableButton;
import com.sty.ne.skin_library.views.SkinnableImageView;
import com.sty.ne.skin_library.views.SkinnableLinearLayout;
import com.sty.ne.skin_library.views.SkinnableRelativeLayout;
import com.sty.ne.skin_library.views.SkinnableTextView;

import androidx.appcompat.app.AppCompatViewInflater;

/**
 * 自定义控件加载器（可以考虑该类不被继承）
 */
public class CustomAppCompatViewInflater extends AppCompatViewInflater {

    private String name; //控件名
    private Context context; //上下文
    private AttributeSet attrs; //某控件对应所有属性

    public CustomAppCompatViewInflater(Context context) {
        this.context = context;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttrs(AttributeSet attrs) {
        this.attrs = attrs;
    }

    public View autoMatch() {
        View view = null;
        switch (name) {
            case "TextView":
                //view = super.createTextView(context, attrs); //源码写法
                view = new SkinnableTextView(context, attrs);
                this.verifyNotNull(view, name);
                break;
            case "ImageView":
                view = new SkinnableImageView(context, attrs);
                this.verifyNotNull(view, name);
                break;
            case "Button":
                view = new SkinnableButton(context, attrs);
                this.verifyNotNull(view, name);
                break;
            case "LinearLayout":
                view = new SkinnableLinearLayout(context, attrs);
                this.verifyNotNull(view, name);
                break;
            case "RelativeLayout":
                view = new SkinnableRelativeLayout(context, attrs);
                this.verifyNotNull(view, name);
                break;
            default:
                break;
        }
        return view;
    }

    /**
     * 校验控件不为空（源码方法，由于private修饰，只能复制过来了。为了代码健壮，可有可无）
     * @param view 被校验控件，如AppCompatTextView extends TextView
     * @param name 控件名，如：ImageView
     */
    private void verifyNotNull(View view, String name) {
        if (view == null) {
            throw new IllegalStateException(this.getClass().getName() + " asked to inflate view for <"
                    + name + ">, but returned null");
        }
    }
}
