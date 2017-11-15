/*
 * Copyright 2009 ZXing authors
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
 * The purpose of this class hierarchy is to abstract different bitmap implementations across
 * platforms into a standard interface for requesting greyscale luminance values. The interface
 * only provides immutable methods; therefore crop and rotation create copies. This is to ensure
 * that one Reader does not modify the original luminance source and leave it in an unknown state
 * for other Readers in the chain.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public abstract class LuminanceSource {

    private final int width;
    private final int height;

    protected LuminanceSource(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Fetches one row of luminance data from the underlying platform's bitmap. Values range from
     * 0 (black) to 255 (white). Because Java does not have an unsigned byte type, callers will have
     * to bitwise and with 0xff for each value. It is preferable for implementations of this method
     * to only fetch this row rather than the whole image, since no 2D Readers may be installed and
     * getMatrix() may never be called.
     *
     * @param y The row to fetch, which must be in [0,getHeight())
     * @param row An optional preallocated array. If null or too small, it will be ignored.
     *            Always use the returned object, and ignore the .length of the array.
     * @return An array containing the luminance data.
     */
    public abstract byte[] getRow(int y, byte[] row);

    /**
     * Fetches luminance data for the underlying bitmap. Values should be fetched using:
     * {@code int luminance = array[y * width + x] & 0xff}
     *
     * @return A row-major 2D array of luminance values. Do not use result.length as it may be
     *         larger than width * height bytes on some platforms. Do not modify the contents
     *         of the result.
     */
    public abstract byte[] getMatrix();

    /**
     * @return The width of the bitmap.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * @return The height of the bitmap.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * @return Whether this subclass supports cropping.
     */
    public boolean isCropSupported() {
        return false;
    }

    /**
     * Returns a new object with cropped image data. Implementations may keep a reference to the
     * original data rather than a copy. Only callable if isCropSupported() is true.
     *
     * @param left The left coordinate, which must be in [0,getWidth())
     * @param top The top coordinate, which must be in [0,getHeight())
     * @param width The width of the rectangle to crop.
     * @param height The height of the rectangle to crop.
     * @return A cropped version of this object.
     */
    public LuminanceSource crop(int left, int top, int width, int height) {
        throw new UnsupportedOperationException("This luminance source does not support cropping.");
    }

    /**
     * @return Whether this subclass supports counter-clockwise rotation.
     */
    public boolean isRotateSupported() {
        return false;
    }

    /**
     * Returns a new object with rotated image data by 90 degrees counterclockwise.
     * Only callable if {@link #isRotateSupported()} is true.
     *
     * @return A rotated version of this object.
     */
    public LuminanceSource rotateCounterClockwise() {
        throw new UnsupportedOperationException("This luminance source does not support rotation by 90 degrees.");
    }

    /**
     * Returns a new object with rotated image data by 45 degrees counterclockwise.
     * Only callable if {@link #isRotateSupported()} is true.
     *
     * @return A rotated version of this object.
     */
    public LuminanceSource rotateCounterClockwise45() {
        throw new UnsupportedOperationException("This luminance source does not support rotation by 45 degrees.");
    }

    @Override
    public final String toString() {
        byte[] row = new byte[width];
        StringBuilder result = new StringBuilder(height * (width + 1));
        for (int y = 0; y < height; y++) {
            row = getRow(y, row);
            for (int x = 0; x < width; x++) {
                int luminance = row[x] & 0xFF;
                char c;
                if (luminance < 0x40) {
                    c = '#';
                } else if (luminance < 0x80) {
                    c = '+';
                } else if (luminance < 0xC0) {
                    c = '.';
                } else {
                    c = ' ';
                }
                result.append(c);
            }
            result.append('\n');
        }
        return result.toString();
    }

}
