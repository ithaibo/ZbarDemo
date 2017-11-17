package com.journeyapps.barcodescanner;


import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

/**
 * This contains the result of a barcode scan.
 *
 * This class delegate all read-only fields of {@link com.google.zxing.Result},
 * and adds a bitmap with scanned barcode.
 */
public class BarcodeResult {
    private static final float PREVIEW_LINE_WIDTH = 4.0f;
    private static final float PREVIEW_DOT_WIDTH = 10.0f;

    protected Result mResult;
    protected SourceData sourceData;

    private final int mScaleFactor = 2;

    public BarcodeResult(Result result, SourceData sourceData) {
        this.mResult = result;
        this.sourceData = sourceData;
    }

    /**
     * @return wrapped {@link com.google.zxing.Result}
     */
    public Result getResult() {
        return mResult;
    }

    /**
     * @return {@link Bitmap} with barcode preview
     */
    public Bitmap getBitmap() {
        return sourceData.getBitmap(mScaleFactor);
    }

    /**
     *
     * @return Bitmap preview scale factor
     */
    public int getBitmapScaleFactor(){
        return mScaleFactor;
    }

    /**
     * @return raw text encoded by the barcode
     * @see Result#getText()
     */
    public String getText() {
        return mResult.getText();
    }

    /**
     * @return raw bytes encoded by the barcode, if applicable, otherwise {@code null}
     * @see Result#getRawBytes()
     */
    public byte[] getRawBytes() {
        return mResult.getRawBytes();
    }


    /**
     * @return {@link BarcodeFormat} representing the format of the barcode that was decoded
     * @see Result#getBarcodeFormat()
     */
    public BarcodeFormat getBarcodeFormat() {
        return mResult.getBarcodeFormat();
    }

    public long getTimestamp() {
        return mResult.getTimestamp();
    }

    @Override
    public String toString() {
        return mResult.getText();
    }
}
