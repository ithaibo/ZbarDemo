package com.andy.zbardemo.decode;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.andy.zbardemo.utils.CameraManager;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;

/**
 * Created by Andy on 2017/11/14.
 */

public class ZbarManager {
    private final String TAG = "ZbarManager";
    private CameraManager cameraManager;
    private Camera camera;
    private Rect mCropRect = null;
    private ImageScanner mImageScanner = null;
    private Handler autoFocusHandler;

    public static final int SCAN_MODE_SINGLE = 1;
    public static final int SCAN_MODE_CONTINUE = 2;
    private int scanMode = SCAN_MODE_SINGLE;
    private long autoFocusRate = 200;
    private CropRectGetter cropRectGetter;
    //    private DecoderThread decoderThread;
    private Handler resultHandler;

    public void init(Context context, Handler resultHandler, CropRectGetter cropRectGetter) {
        initImageScanner();
        this.resultHandler = resultHandler;
        this.cropRectGetter = cropRectGetter;
//        decoderThread = new DecoderThread(mResultHandler, cropRectGetter);

        autoFocusHandler = new Handler(Looper.getMainLooper());
        this.cameraManager = new CameraManager(context);
        boolean driverOpened = false;
        try {
            this.cameraManager.openDriver();
            driverOpened = true;
        } catch (IOException e) {
            Log.e(TAG, "open camera driver failed.");
            e.printStackTrace();
        }

        if (driverOpened) {
            this.camera = this.cameraManager.getCamera();
        }
    }


    private void initImageScanner() {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
        SymbolSet symbolSet = mImageScanner.getResults();
        for (Symbol symbol : symbolSet) {
            mImageScanner.setConfig(symbol.getType(), Config.ENABLE, 0);
        }
        mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
    }

    public Camera getCamera() {
        return camera;
    }

    private DecodeResultCallback mResultHandler = new DecodeResultCallback() {
        @Override
        public void onResult(String data) {
            if (TextUtils.isEmpty(data)) {
                //TODO parse fail
//                resultHandler.obtainMessage(DecoderThread.MSG_WHAT_DECODE_FAILED).sendToTarget();
                camera.startPreview();
            } else {
                //TODO handle result
                if (scanMode != SCAN_MODE_CONTINUE) {
                    if (camera != null) {
                        stopDecode();
                    }
                    //invoke callback
                    resultHandler.obtainMessage(DecoderThread.MSG_WHAT_DECODE_SUCCESS, data).sendToTarget();
                } else {
                    camera.startPreview();
                }
            }
        }
    };

    private Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, autoFocusRate);
        }
    };

    public Camera.AutoFocusCallback getAutoFocusCB() {
        return autoFocusCB;
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (cameraManager.isOpen() && !cameraManager.getPreviewing()) {
                camera.autoFocus(autoFocusCB);
            }
        }
    };

    private Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        private byte[] rotatedData;
        private String lastResult = null;
        private long lastTime;

        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.i(TAG, "preview callback invoked.");
            if (mCropRect == null) {
                if (cropRectGetter != null) {
                    mCropRect = cropRectGetter.getCropRect();
                } else {
                    Log.e(TAG, "crop rect getter is null.");
                }
            }

            int[] cropPosition = new int[4];
            if (mCropRect != null) {
                cropPosition[0] = mCropRect.left;
                cropPosition[1] = mCropRect.top;
                cropPosition[2] = mCropRect.width();
                cropPosition[3] = mCropRect.height();

                if (data == null || data.length <= 0) {
                    return;
                }
                Camera.Size size = camera.getParameters().getPreviewSize();

                Log.i(TAG, "data length = " + data.length);
                if (rotatedData == null || rotatedData.length!= data.length) {
                    rotatedData = new byte[data.length];
                }
                for (int y = 0; y < size.height; y++) {
                    for (int x = 0; x < size.width; x++)
                        rotatedData[x * size.height + size.height - y - 1] = data[x
                                + y * size.width];
                }
//                data = null;
                if (rotatedData == null || rotatedData.length <= 0) {
                    return;
                }

                int sourceWidth = size.width;
                int sourceHeight = size.height;

                Image barcode = new Image(sourceWidth, sourceHeight, "Y800");
                barcode.setData(rotatedData);
                if (cropPosition != null && cropPosition.length != 4) {
                    barcode.setCrop(cropPosition[0], cropPosition[1], cropPosition[2], cropPosition[3]);
                }

                if (barcode == null) {
                    Log.e(TAG, "Image instance create filed, cannot parse.");
                    return;
                }
                if (mImageScanner == null) {
                    Log.e(TAG, "image scanner is null, cannot parse data");
                }

                Log.i(TAG, "main thread = " + Looper.getMainLooper().getThread().getId());
                Log.i(TAG, "work thread = " + Thread.currentThread().getId());
                int result = mImageScanner.scanImage(barcode);

                if (result != 0) {
                    stopDecode();
                    String resultStr = null;
                    SymbolSet syms = mImageScanner.getResults();
                    for (Symbol sym : syms) {
//                        if (!CodeFormatter.getInstance().isFormatterAvailable(sym.getType())) {
//                            continue;
//                        }
                        Log.i(TAG, "decode start ...");
                        resultStr = sym.getData();
                    }

                    if (TextUtils.isEmpty(resultStr)) {
                        Log.e(TAG, "decode fail");
                        mResultHandler.onResult(null);
                    } else {
                        long timeNow = System.currentTimeMillis();
                        if (!TextUtils.equals(lastResult, resultStr) || autoFocusRate< (timeNow - lastTime)) {
                            lastResult = resultStr;
                            mResultHandler.onResult(resultStr);
                            Log.i(TAG, "decode success, new data: " + resultStr);
                        } else {
                            mResultHandler.onResult(null);
                            Log.e(TAG, "decode success, duplicated data: " + lastResult);
                        }
                        lastTime = System.currentTimeMillis();
                    }
                } else {
                    Log.e(TAG, "no result");
                    mResultHandler.onResult(null);
                }
            } else {
                Log.e(TAG, "crop rect return null.");
            }
        }
    };

    public Camera.PreviewCallback getPreviewCb() {
//        return decoderThread.getPreviewCb();
        return previewCb;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void decodeSingle() {
        Log.i(TAG, "decodeSingle invoked.");
        scanMode = SCAN_MODE_SINGLE;
        cameraManager.startPreview(getPreviewCb(), getAutoFocusCB());
//        decoderThread.start();
    }

    public void decodeContinue() {
        //TODO
    }

    public void stopDecode() {
        try {
            cameraManager.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            stopDecode();
            camera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
