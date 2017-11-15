/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing;


/**
 * <p>Encapsulates the result of decoding a barcode within an image.</p>
 *
 * @author Sean Owen
 */
public final class Result {

    private final String text;
    private final byte[] rawBytes;
    private final int numBits;
    private final BarcodeFormat format;
    private final long timestamp;

    public Result(String text,
                  byte[] rawBytes,
                  BarcodeFormat format) {
        this(text, rawBytes, format, System.currentTimeMillis());
    }

    public Result(String text,
                  byte[] rawBytes,
                  BarcodeFormat format,
                  long timestamp) {
        this(text, rawBytes, rawBytes == null ? 0 : 8 * rawBytes.length, format, timestamp);
    }

    public Result(String text,
                  byte[] rawBytes,
                  int numBits,
                  BarcodeFormat format,
                  long timestamp) {
        this.text = text;
        this.rawBytes = rawBytes;
        this.numBits = numBits;
        this.format = format;
        this.timestamp = timestamp;
    }

    /**
     * @return raw text encoded by the barcode
     */
    public String getText() {
        return text;
    }

    /**
     * @return raw bytes encoded by the barcode, if applicable, otherwise {@code null}
     */
    public byte[] getRawBytes() {
        return rawBytes;
    }

    /**
     * @return how many bits of {@link #getRawBytes()} are valid; typically 8 times its length
     * @since 3.3.0
     */
    public int getNumBits() {
        return numBits;
    }

    /**
     * @return {@link BarcodeFormat} representing the format of the barcode that was decoded
     */
    public BarcodeFormat getBarcodeFormat() {
        return format;
    }


    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return text;
    }

}
