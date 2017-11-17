package com.ithaibo.zbar;

import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;

/**
 * A class for decoding images.
 *
 * A decoder contains all the configuration required for the binarization and decoding process.
 *
 * The actual decoding should happen on a dedicated thread.
 */
public interface Decoder {

    /**
     * Create a new Decoder with the specified Reader.
     *
     * It is recommended to use an instance of MultiFormatReader in most cases.
     *
     * @param reader the reader
     */

    /**
     * Given an image source, attempt to decode the barcode.
     *
     * Must not raise an exception.
     *
     * @param source the image source
     * @return a Result or null
     */
    Result decode(LuminanceSource source);
}
