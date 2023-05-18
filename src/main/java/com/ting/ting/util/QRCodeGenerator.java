package com.ting.ting.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;

public class QRCodeGenerator {

    public static byte[] generateQRCodeImageBytes(String linkedUrl) throws Exception{
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(linkedUrl, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        return imageBytes;
    }
}
