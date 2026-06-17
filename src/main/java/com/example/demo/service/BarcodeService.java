package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Renders 1-D barcodes (Code-128 or EAN-13) as PNG images using ZXing.
 */
@Service
public class BarcodeService {

    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 90;

    /** Encodes {@code content} using the given symbology as a PNG. */
    public byte[] pngFor(String content, BarcodeType type) {
        return pngFor(content, type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public byte[] pngFor(String content, BarcodeType type, int width, int height) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Barcode content must not be blank");
        }
        BarcodeType resolved = type == null ? BarcodeType.CODE_128 : type;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 4);

            BitMatrix matrix = switch (resolved) {
                case CODE_128 -> new Code128Writer()
                        .encode(content, BarcodeFormat.CODE_128, width, height, hints);
                case EAN_13 -> new EAN13Writer()
                        .encode(toEan13Payload(content), BarcodeFormat.EAN_13, width, height, hints);
            };
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate barcode for '" + content + "'", e);
        }
    }

    /**
     * EAN-13 requires exactly 12 numeric digits (a 13th check digit is computed by
     * the writer). We strip non-digits from the content and, if there are fewer
     * than 12 digits, deterministically pad using the content's hash so the same
     * input always yields the same barcode.
     */
    String toEan13Payload(String content) {
        StringBuilder digits = new StringBuilder(content.replaceAll("\\D", ""));
        if (digits.length() >= 12) {
            return digits.substring(0, 12);
        }
        int hash = Math.abs(content.hashCode());
        String filler = String.format("%012d", hash);
        digits.append(filler);
        return digits.substring(0, 12);
    }
}
