package com.lancast.lancast.core;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

/**
 * Service to generate QR codes as JavaFX Images.
 */
public class QRService {

    /**
     * Generates a QR Code image for the given text.
     *
     * @param text   The text to encode (e.g., URL).
     * @param width  The width of the QR code.
     * @param height The height of the QR code.
     * @return A JavaFX Image containing the QR code.
     * @throws WriterException If an error occurs during generation.
     */
    public Image generateQRCode(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Convert BufferedImage to JavaFX Image
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
