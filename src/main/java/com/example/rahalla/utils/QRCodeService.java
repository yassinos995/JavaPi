package com.example.rahalla.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeService {
    private static final int DEFAULT_SIZE = 300;
    private static final String DEFAULT_FORMAT = "PNG";
    private static final String CHARSET = "UTF-8";

    public static BufferedImage generateQRCode(String content) throws WriterException, IOException {
        return generateQRCode(content, DEFAULT_SIZE);
    }

    public static BufferedImage generateQRCode(String content, int size) throws WriterException, IOException {
        validateInput(content, size);

        try {
            Map<EncodeHintType, Object> hints = createEncodingHints();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException | IllegalArgumentException e) {
            System.out.println("Failed to generate QR code" + e.getMessage());
            throw new WriterException("Failed to generate QR code: " + e.getMessage());
        }
    }

    private static void validateInput(String content, int size) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("QR code content cannot be null or empty");
        }

        if (content.length() > 2953) { // Maximum capacity for QR code with UTF-8
            throw new IllegalArgumentException("Content length exceeds maximum QR code capacity");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }

        if (size > 4096) { // Reasonable maximum size
            throw new IllegalArgumentException("Size exceeds maximum allowed value (4096)");
        }
    }

    private static Map<EncodeHintType, Object> createEncodingHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Highest error correction
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1); // Quiet zone margin
        return hints;
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}