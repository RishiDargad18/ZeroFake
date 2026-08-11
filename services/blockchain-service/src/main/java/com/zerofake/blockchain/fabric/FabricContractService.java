package com.zerofake.blockchain.fabric;

import com.zerofake.blockchain.config.FabricProperties;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.Proposal;
import org.springframework.stereotype.Service;

/**
 * Resolves the ZeroFake smart contract on the configured channel.
 */
@Service
@RequiredArgsConstructor
public class FabricContractService {

    private final FabricGatewayService fabricGatewayService;
    private final FabricProperties fabricProperties;

    private final Object lock = new Object();

    private volatile Contract contract;

    public Contract getContract() {

        Contract current = contract;

        if (current != null) {
            return current;
        }

        synchronized (lock) {

            if (contract != null) {
                return contract;
            }

            Network network = fabricGatewayService
                    .getGateway()
                    .getNetwork(fabricProperties.getChannelName());

            contract = network.getContract(fabricProperties.getChaincodeName());

            return contract;
        }
    }

    /**
     * Creates a proposal for the named chaincode transaction. The caller adds
     * arguments, endorses and submits it.
     */
    public Proposal.Builder newProposal(String transactionName) {
        return getContract().newProposal(transactionName);
    }
}
