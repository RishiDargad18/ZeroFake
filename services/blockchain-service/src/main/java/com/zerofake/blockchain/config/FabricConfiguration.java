package com.zerofake.blockchain.config;

import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FabricConfiguration {

    private final FabricProperties fabricProperties;

    public FabricConfiguration(FabricProperties fabricProperties) {
        this.fabricProperties = fabricProperties;
    }

    @Bean
    public Gateway gateway() {
        throw new UnsupportedOperationException(
                "Will be implemented during Fabric Gateway integration."
        );
    }

    @Bean
    public Network network() {
        throw new UnsupportedOperationException(
                "Will be implemented during Fabric Gateway integration."
        );
    }

    @Bean
    public Contract contract() {
        throw new UnsupportedOperationException(
                "Will be implemented during Fabric Gateway integration."
        );
    }

}