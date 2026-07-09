package com.zerofake.product.entity;

import com.zerofake.product.constant.BlockchainStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Product extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String productCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank
    @Size(max = 150)
    @Column(name = "brand", nullable = false, length = 150)
    private String brand;

    @NotNull
    @Column(name = "manufacturer_id", nullable = false)
    private UUID manufacturerId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductCategory category;

    @Size(max = 500)
    @Column(name = "qr_code_path", length = 500)
    private String qrCodePath;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "image_file_name", length = 255)
    private String imageFileName;

    @Size(max = 100)
    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @Column(name = "image_size")
    private Long imageSize;

    @Builder.Default
    private Boolean active = true;

    @Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "blockchain_status", nullable = false, length = 20)
    private BlockchainStatus blockchainStatus = BlockchainStatus.PENDING;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ProductBatch> batches = new ArrayList<>();
}