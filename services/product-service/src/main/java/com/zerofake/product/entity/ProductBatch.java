package com.zerofake.product.entity;

import com.zerofake.product.constant.BatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "product_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductBatch extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String batchNumber;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private LocalDate manufactureDate;

    @Column
    private LocalDate expiryDate;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantityProduced;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer availableQuantity;

    @Size(max = 255)
    @Column(length = 255)
    private String manufacturingLocation;

    @Size(max = 500)
    @Column(length = 500)
    private String remarks;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status = BatchStatus.CREATED;
}