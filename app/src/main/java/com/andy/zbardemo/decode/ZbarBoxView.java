package com.andy.zbardemo.decode;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.andy.zbardemo.widget.CameraPreview;

/**
 * Created by Andy on 2017/11/14.
 */

public class ZbarBoxView extends FrameLayout {
    private CameraPreview preview;

    public ZbarBoxView(@NonNull Context context) {
        super(context);
    }

    public ZbarBoxView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZbarBoxView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(CameraPreview preview) {
        this.preview = preview;
        //
    }


}
