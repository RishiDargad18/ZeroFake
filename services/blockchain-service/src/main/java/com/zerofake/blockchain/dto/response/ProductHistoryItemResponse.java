package com.zerofake.blockchain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductHistoryItemResponse {

    private UUID manufacturerId;

    private UUID currentOwnerId;

    private String currentOwnerRole;

    private String productStatus;

    private Boolean verified;

    private String createdAt;

    private String updatedAt;

}