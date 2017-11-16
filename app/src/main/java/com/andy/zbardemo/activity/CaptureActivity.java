package com.andy.zbardemo.activity;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andy.zbardemo.R;
import com.andy.zbardemo.decode.CropRectGetter;
import com.andy.zbardemo.decode.DecoderThread;
import com.andy.zbardemo.decode.ZbarManager;
import com.andy.zbardemo.utils.SoundUtils;
import com.andy.zbardemo.widget.CameraPreview;


public class CaptureActivity extends Activity {

    private CameraPreview mPreview;
    private TextView scanResult;
    private FrameLayout scanPreview;
    private Button scanRestart;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private TranslateAnimation animation;
    private ZbarManager zbarManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        SoundUtils.getInstance().init(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById();
        addEvents();
        initViews();
    }

    private void findViewById() {
        scanPreview = (FrameLayout) findViewById(R.id.capture_preview);
        scanResult = (TextView) findViewById(R.id.capture_scan_result);
        scanRestart = (Button) findViewById(R.id.capture_restart_scan);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
    }

    private void addEvents() {
        scanRestart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                animation.start();
                zbarManager.decodeSingle();
            }
        });
    }

    private Handler resultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DecoderThread.MSG_WHAT_DECODE_SUCCESS:
                    Log.i("CaptureActivity", "decode success, data: " + msg.obj);
                    SoundUtils.getInstance().playSuccess();
                    Toast.makeText(CaptureActivity.this, "data: " + msg.obj, Toast.LENGTH_SHORT).show();
                    scanResult.setText((String)msg.obj);
                    animation.cancel();
                    break;
//                case DecoderThread.MSG_WHAT_DECODE_FAILED:
//                    Log.i("CaptureActivity", "decode fail.");
//                    break;
            }
        }
    };

    public void onResult(final String data) {
        //handle result
        SoundUtils.getInstance().playSound(R.raw.success);
        scanResult.post(new Runnable() {
            @Override
            public void run() {
                animation.cancel();
                scanResult.setText(data);
                Toast.makeText(CaptureActivity.this, "result: " + data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        zbarManager = new ZbarManager();
        zbarManager.init(this, resultHandler, cropRectGetter);
        mPreview = new CameraPreview(this, zbarManager.getCamera(), zbarManager.getPreviewCb(), zbarManager.getAutoFocusCB());
        scanPreview.addView(mPreview);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.85f);
        animation.setDuration(3000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);

        zbarManager.decodeSingle();
    }

    public void onPause() {
        super.onPause();
        zbarManager.stopDecode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zbarManager.onDestroy();
    }

    private CropRectGetter cropRectGetter = new CropRectGetter() {
        @Override
        public Rect getCropRect() {
            return initCrop();
        }
    };

    /**
     * 初始化截取的矩形区域
     */
    private Rect initCrop() {
        int cameraWidth = zbarManager.getCameraManager().getCameraResolution().y;
        int cameraHeight = zbarManager.getCameraManager().getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getMeasuredWidth();
        int containerHeight = scanContainer.getMeasuredHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        return new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
