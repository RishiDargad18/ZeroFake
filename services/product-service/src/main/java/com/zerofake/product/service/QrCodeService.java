package com.zerofake.product.service;

import java.util.UUID;

public interface QrCodeService {

    /**
     * Generates and stores the QR code image for a product.
     *
     * @return the storage path of the generated image, relative to the
     *         configured QR storage directory
     */
    String generateForProduct(UUID productId);

    /**
     * Reads a previously generated QR code image.
     *
     * @param qrCodePath the path recorded on the product
     * @return the PNG bytes of the image
     */
    byte[] readQrCode(String qrCodePath);
}
