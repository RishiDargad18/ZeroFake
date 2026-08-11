package com.zerofake.product.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.zerofake.product.exception.ResourceNotFoundException;
import com.zerofake.product.service.QrCodeService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Generates the QR code that is physically attached to a product.
 *
 * <p>The encoded payload is the product's UUID and nothing else. The QR code is
 * therefore only a pointer — scanning it proves nothing on its own, and every
 * scan must still be verified against the blockchain. Encoding richer data would
 * invite the mistake of trusting the label instead of the ledger.
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeServiceImpl.class);

    private static final String IMAGE_FORMAT = "PNG";

    private final Path storageDirectory;
    private final int imageSize;

    public QrCodeServiceImpl(
            @Value("${zerofake.qr.storage-directory}") String storageDirectory,
            @Value("${zerofake.qr.image-size}") int imageSize
    ) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
        this.imageSize = imageSize;
    }

    @PostConstruct
    void createStorageDirectory() {
        try {
            Files.createDirectories(storageDirectory);
            log.info("QR code storage directory: {}", storageDirectory);
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Unable to create QR code storage directory: " + storageDirectory, ex
            );
        }
    }

    @Override
    public String generateForProduct(UUID productId) {

        String fileName = productId + ".png";
        Path target = storageDirectory.resolve(fileName);

        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    productId.toString(),
                    BarcodeFormat.QR_CODE,
                    imageSize,
                    imageSize,
                    Map.of(
                            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                            EncodeHintType.MARGIN, 1
                    )
            );

            MatrixToImageWriter.writeToPath(matrix, IMAGE_FORMAT, target);

            return fileName;

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to generate QR code for product " + productId, ex
            );
        }
    }

    @Override
    public byte[] readQrCode(String qrCodePath) {

        if (qrCodePath == null || qrCodePath.isBlank()) {
            throw new ResourceNotFoundException("No QR code has been generated for this product.");
        }

        // Resolve within the storage directory and reject anything that escapes it.
        Path target = storageDirectory.resolve(qrCodePath).normalize();

        if (!target.startsWith(storageDirectory)) {
            throw new ResourceNotFoundException("QR code not found.");
        }

        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("QR code image is no longer available.");
        }

        try {
            return Files.readAllBytes(target);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read QR code image: " + target, ex);
        }
    }
}
