package com.immortalcrab.qrcode;

import java.util.Base64;
import java.util.Base64.Encoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCode {

    public static void generate(String text, int width, int height, String filePath) throws Exception {
        QRCodeWriter qcWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qcWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    public static ByteArrayInputStream generateByteStream(String text, int width, int height) throws Exception {
        QRCodeWriter qcWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qcWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        var out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public static String generateBase64EncodedString(String text, int width, int height) throws Exception {
        ByteArrayInputStream bais = generateByteStream(text, width, height);
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bais.readAllBytes());
    }
}
