package com.zerofake.product.service.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zerofake.product.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QR code generation.
 *
 * <p>The encoded payload is a deliberate design decision: the product
 * identifier and nothing else. A QR code is trivially copyable, so it is only
 * ever a pointer — authenticity is established against the ledger, never from
 * the label. Encoding richer data here would invite exactly the mistake the
 * platform exists to prevent.
 */
class QrCodeServiceImplTest {

    @TempDir
    Path storageDirectory;

    private QrCodeServiceImpl qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeServiceImpl(storageDirectory.toString(), 300);
        qrCodeService.createStorageDirectory();
    }

    private String decode(byte[] png) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));

        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );

        return new QRCodeReader().decode(bitmap).getText();
    }

    @Nested
    @DisplayName("generation")
    class Generation {

        @Test
        @DisplayName("writes a readable PNG for the product")
        void writesReadablePng() throws Exception {

            UUID productId = UUID.randomUUID();

            String path = qrCodeService.generateForProduct(productId);
            byte[] png = qrCodeService.readQrCode(path);

            assertThat(png).isNotEmpty();
            assertThat(ImageIO.read(new ByteArrayInputStream(png))).isNotNull();
        }

        @Test
        @DisplayName("encodes exactly the product identifier, and nothing else")
        void encodesExactlyTheProductIdentifier() throws Exception {

            UUID productId = UUID.randomUUID();

            String path = qrCodeService.generateForProduct(productId);

            assertThat(decode(qrCodeService.readQrCode(path)))
                    .isEqualTo(productId.toString());
        }

        @Test
        @DisplayName("names the file after the product, so it is regenerable")
        void namesFileAfterProduct() {

            UUID productId = UUID.randomUUID();

            assertThat(qrCodeService.generateForProduct(productId))
                    .isEqualTo(productId + ".png");
        }

        @Test
        @DisplayName("regenerating for the same product overwrites in place")
        void regeneratingOverwritesInPlace() throws Exception {

            UUID productId = UUID.randomUUID();

            qrCodeService.generateForProduct(productId);
            qrCodeService.generateForProduct(productId);

            try (var files = Files.list(storageDirectory)) {
                assertThat(files.count()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("produces a distinct image per product")
        void producesDistinctImagePerProduct() throws Exception {

            UUID first = UUID.randomUUID();
            UUID second = UUID.randomUUID();

            String firstPath = qrCodeService.generateForProduct(first);
            String secondPath = qrCodeService.generateForProduct(second);

            assertThat(decode(qrCodeService.readQrCode(firstPath)))
                    .isNotEqualTo(decode(qrCodeService.readQrCode(secondPath)));
        }
    }

    @Nested
    @DisplayName("reading")
    class Reading {

        @Test
        @DisplayName("reports a product with no QR code as not found")
        void reportsMissingQrCodeAsNotFound() {

            assertThatThrownBy(() -> qrCodeService.readQrCode(null))
                    .isInstanceOf(ResourceNotFoundException.class);

            assertThatThrownBy(() -> qrCodeService.readQrCode("  "))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("reports a deleted image as not found")
        void reportsDeletedImageAsNotFound() throws Exception {

            UUID productId = UUID.randomUUID();
            String path = qrCodeService.generateForProduct(productId);

            Files.delete(storageDirectory.resolve(path));

            assertThatThrownBy(() -> qrCodeService.readQrCode(path))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("refuses to read outside the storage directory")
        void refusesToReadOutsideStorageDirectory() throws Exception {

            // The stored path reaches this method from the database, so it is
            // treated as untrusted input: a traversal must not escape into the
            // wider filesystem.
            Path secret = storageDirectory.getParent().resolve("secret.txt");
            Files.writeString(secret, "not for the web");

            assertThatThrownBy(() -> qrCodeService.readQrCode("../secret.txt"))
                    .isInstanceOf(ResourceNotFoundException.class);

            assertThatThrownBy(() -> qrCodeService.readQrCode("../../../../etc/passwd"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
