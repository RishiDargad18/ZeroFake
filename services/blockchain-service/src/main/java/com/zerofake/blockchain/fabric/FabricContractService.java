package com.zerofake.blockchain.fabric;

import com.zerofake.blockchain.config.FabricProperties;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FabricContractService {

    private final FabricGatewayService fabricGatewayService;

    private final FabricProperties fabricProperties;

    private Contract contract;

    public Contract getContract() throws Exception {

        if (contract != null) {
            return contract;
        }

        Gateway gateway = fabricGatewayService.getGateway();

        Network network = gateway.getNetwork(
                fabricProperties.getChannelName()
        );

        contract = network.getContract(
                fabricProperties.getChaincodeName()
        );

        return contract;
    }
}