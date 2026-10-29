package org.spring.metro.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.spring.metro.models.entity.Ticket;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int DEFAULT_SIZE = 300;

    public byte[] generateTicketQr(Ticket ticket) {

        String payload = String.join("|",
                "METRO",
                String.valueOf(ticket.getTicketId()),
                ticket.getSourceStation().getStationId(),
                ticket.getDestStation().getStationId(),
                String.valueOf(ticket.getTicketType()),
                String.valueOf(ticket.getValidUntil())
        );

        return generate(payload, DEFAULT_SIZE);
    }

    public byte[] generate(String content, int size) {

        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION,
                    ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 2,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );

            BitMatrix matrix = new QRCodeWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return output.toByteArray();

        } catch (WriterException | IOException exception) {
            throw new IllegalStateException(
                    "Failed to generate QR code",
                    exception
            );
        }
    }
}

