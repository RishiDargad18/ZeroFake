package com.zerofake.blockchain.mapper;

import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.entity.BlockchainTransaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BlockchainTransactionMapper {

    BlockchainTransactionResponse toResponse(BlockchainTransaction entity);

    List<BlockchainTransactionResponse> toResponseList(List<BlockchainTransaction> entities);

}