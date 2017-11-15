package com.andy.zbardemo.decode;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * 异步解码
 */
public class DecoderThread {
    private static final String TAG = DecoderThread.class.getSimpleName();
    private final int MSG_WHAT_DECODE = 1;
    private final int MSG_WHAT_PREVIEW_FAILED = 2;
    public static final int MSG_WHAT_DECODE_SUCCESS = 4;
    public static final int MSG_WHAT_DECODE_FAILED = 8;

    private HandlerThread thread;
    private Handler handler;
    private DecodeResultCallback resultCallback;
    private Rect cropRect;
    private boolean running = false;
    private final Object LOCK = new Object();
    private ImageScanner imageScanner;
    private CropRectGetter cropRectGetter;
    private Rect mCropRect;

    private final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == MSG_WHAT_DECODE) {
                ImageSourceData sourceData = (ImageSourceData) message.obj;
                Image image = createImage(sourceData.data, sourceData.size, null);
                decodeImage(image, imageScanner);
            } else if(message.what == MSG_WHAT_PREVIEW_FAILED) {
                // Error already logged. Try again.
                requestNextPreview();
            }
            return true;
        }
    };

    public DecoderThread(DecodeResultCallback resultHandler, CropRectGetter cropRectGetter) {
        Util.validateMainThread();
        this.resultCallback = resultHandler;
        this.cropRectGetter = cropRectGetter;
        initImageScanner();
        initCodeFormatter();
    }

    private void initImageScanner() {
        imageScanner = new ImageScanner();
        imageScanner.setConfig(0, Config.X_DENSITY, 3);
        imageScanner.setConfig(0, Config.Y_DENSITY, 3);
        SymbolSet symbolSet = imageScanner.getResults();
        for (Symbol symbol : symbolSet) {
            imageScanner.setConfig(symbol.getType(), Config.ENABLE, 0);
        }
//        imageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 0);
        imageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
    }

    private void initCodeFormatter() {
        CodeFormatter codeFormatter = CodeFormatter.getInstance();
        codeFormatter.addAvailableFormatter(Symbol.CODE128);
        codeFormatter.addAvailableFormatter(Symbol.CODE39);
        codeFormatter.addAvailableFormatter(Symbol.CODE93);
        codeFormatter.addAvailableFormatter(Symbol.CODABAR);
    }

    /**
     * Start decoding.
     *
     * This must be called from the UI thread.
     */
    public void start() {
        Util.validateMainThread();

        thread = new HandlerThread(TAG);
        thread.start();
        handler = new Handler(thread.getLooper(), callback);
        running = true;
        requestNextPreview();
    }

    /**
     * Stop decoding.
     *
     * This must be called from the UI thread.
     */
    public void stop() {
        Util.validateMainThread();

        synchronized (LOCK) {
            running = false;
            handler.removeCallbacksAndMessages(null);
            thread.quit();
        }
    }

    private Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
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
            if (mCropRect!=null) {
                cropPosition[0] = mCropRect.left;
                cropPosition[1] = mCropRect.top;
                cropPosition[2] = mCropRect.width();
                cropPosition[3] = mCropRect.height();

                ImageSourceData sourceData = new ImageSourceData();
                sourceData.data = data;
                sourceData.size = camera.getParameters().getPreviewSize();
                sourceData.cropPosition = cropPosition; //TODO: add

                onPreviewSuccess(sourceData);
            } else {
                Log.e(TAG, "crop rect return null.");
                onPreviewError();
            }

        }

        private void onPreviewSuccess(ImageSourceData sourceData) {
            Log.i(TAG, "onPreviewSuccess invoked");
            synchronized (LOCK) {
                if (running) {
                    // Post to our thread.
                    handler.obtainMessage(MSG_WHAT_DECODE, sourceData).sendToTarget();
                }
            }
        }

        private void onPreviewError() {
            Log.i(TAG, "onPreviewError invoked");
            synchronized (LOCK) {
                if (running) {
                    // Post to our thread.
                    handler.obtainMessage(MSG_WHAT_PREVIEW_FAILED).sendToTarget();
                }
            }
        }

    };

    public Camera.PreviewCallback getPreviewCb() {
        return previewCb;
    }

    private void requestNextPreview() {
        //TODO: start preview again
    }

    private Image createImage(byte[] data, Camera.Size size, int[] cropPosition) {
        if (data == null || data.length<=0) {
            return null;
        }

        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x
                        + y * size.width];
        }
        data = null;
        if (rotatedData == null || rotatedData.length<=0) {
            return  null;
        }

        int sourceWidth = size.width;
        int sourceHeight = size.height;

        Image barcode = new Image(sourceWidth, sourceHeight, "Y800");
        barcode.setData(rotatedData);
        if (cropPosition !=null && cropPosition.length!=4) {
            barcode.setCrop(cropPosition[0], cropPosition[1], cropPosition[2], cropPosition[3]);
        }

        return barcode;
    }

    private void decodeImage(Image barcode, ImageScanner imageScanner) {
        if (barcode == null) {
            Log.e(TAG, "Image instance create filed, cannot parse.");
            return;
        }
        if (imageScanner == null) {
            Log.e(TAG, "image scanner is null, cannot parse data");
        }

        Log.i(TAG, "main thread = " + Looper.getMainLooper().getThread().getId());
        Log.i(TAG, "work thread = " + Thread.currentThread().getId());
        int result = imageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = imageScanner.getResults();
            for (Symbol sym : syms) {
                if (!CodeFormatter.getInstance().isFormatterAvailable(sym.getType())) {
                    continue;
                }
                Log.i(TAG, "decode start ...");
                resultStr = sym.getData();
            }
        }

        if (TextUtils.isEmpty(resultStr)) {
            Log.e(TAG, "decode fail");
        } else {
            Log.i(TAG, "decode success: " + resultStr);
        }

        resultCallback.onResult(resultStr);
    }

    private class ImageSourceData {
        private Camera.Size size;
        private byte[] data;
        int[] cropPosition;
    }
}
