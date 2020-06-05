package com.sty.ne.dynamicskinpeeler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sty.ne.dynamicskinpeeler.utils.PermissionUtils;
import com.sty.ne.skin_library.base.SkinActivity;
import com.sty.ne.skin_library.utils.PreferencesUtils;

import java.io.File;

/**
 * 如果图标有固定的尺寸，不需要更改，那么drawable更加合适
 * 如果需要变大变小，有动画的，放在mipmap中能有更高的质量
 */
public class MainActivity extends SkinActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String skinName = "sty.skin";
    private String skinPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "sty" + File.separator + skinName;
    private Button btnChangeSkin;
    private Button btnSetSkinDefault;
    private Button btnJumpSelf;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }

        initViews();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initViews() {
        btnChangeSkin = findViewById(R.id.btn_change_skin);
        btnSetSkinDefault = findViewById(R.id.btn_set_skin_default);
        btnJumpSelf = findViewById(R.id.btn_jump_self);

        btnChangeSkin.setOnClickListener(this);
        btnSetSkinDefault.setOnClickListener(this);
        btnJumpSelf.setOnClickListener(this);

        if(skinName.equals(PreferencesUtils.getString(this, "currentSkin"))){
            changeSkin();
        }else {
            setSkinDefault();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_skin:
                changeSkin();
                break;
            case R.id.btn_set_skin_default:
                setSkinDefault();
                break;
            case R.id.btn_jump_self:
                jumpSelf();
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean openChangeSkin() {
        return true;
    }

    private void jumpSelf() {
        startActivity(new Intent(this, this.getClass()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSkinDefault() {
        //先判断当前皮肤，避免重复操作
        if(!("default").equals(PreferencesUtils.getString(this, "currentSkin"))) {
            Log.d(TAG, "-----------------start-----------------");
            long start = System.currentTimeMillis();

            defaultSkin(R.color.colorPrimary);
            PreferencesUtils.putString(this, "currentSkin", "default");

            long end = System.currentTimeMillis() - start;
            Log.d(TAG, "还原耗时（毫秒）：" + end);
            Log.d(TAG, "-----------------end-----------------");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeSkin() {
        //先判断当前皮肤，避免重复操作
        if(!(skinName).equals(PreferencesUtils.getString(this, "currentSkin"))) {
            Log.d(TAG, "-----------------start-----------------");
            long start = System.currentTimeMillis();

            skinDynamic(skinPath, R.color.skin_item_color);
            PreferencesUtils.putString(this, "currentSkin", skinName);

            long end = System.currentTimeMillis() - start;
            Log.d(TAG, "换肤耗时（毫秒）：" + end);
            Log.d(TAG, "-----------------end-----------------");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_PERMISSIONS_CODE) {
            if (!PermissionUtils.verifyPermissions(grantResults)) {
                PermissionUtils.showMissingPermissionDialog(this);
            } else {
                initViews();
            }
        }
    }
}
