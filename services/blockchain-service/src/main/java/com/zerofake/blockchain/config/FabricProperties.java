package com.zerofake.blockchain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fabric")
public class FabricProperties {

    private String mspId;

    private String channelName;

    private String chaincodeName;

    private String cryptoPath;

    private String certPath;

    private String keyDirectoryPath;

    private String tlsCertPath;

    private String peerEndpoint;

    private String peerHostAlias;

}