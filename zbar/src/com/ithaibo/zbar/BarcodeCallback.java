package com.journeyapps.barcodescanner;

/**
 * Callback that is notified when a barcode is scanned.
 */
public interface BarcodeCallback {
    /**
     * Barcode was successfully scanned.
     *
     * @param result the result
     */
    void barcodeResult(BarcodeResult result);
}
