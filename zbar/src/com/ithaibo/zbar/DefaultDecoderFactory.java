package com.ithaibo.zbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.util.Collection;
import java.util.Map;

/**
 * DecoderFactory that creates a MultiFormatReader with specified hints.
 */
public class DefaultDecoderFactory implements DecoderFactory {
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> hints;
    private String characterSet;
    private boolean inverted;

    public DefaultDecoderFactory() {
    }

    public DefaultDecoderFactory(Collection<BarcodeFormat> decodeFormats, Map<DecodeHintType, ?> hints, String characterSet, boolean inverted) {
        this.decodeFormats = decodeFormats;
        this.hints = hints;
        this.characterSet = characterSet;
        this.inverted = inverted;
    }

    @Override
    public Decoder createDecoder(Map<DecodeHintType, ?> baseHints) {
        return new ZbarDecoder();
    }
}
