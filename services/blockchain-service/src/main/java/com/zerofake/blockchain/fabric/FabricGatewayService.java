package com.zerofake.blockchain.fabric;

import com.zerofake.blockchain.config.FabricProperties;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Gateway;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FabricGatewayService {

    private final FabricProperties fabricProperties;

    private final FabricIdentityService fabricIdentityService;

    private Gateway gateway;

    private ManagedChannel channel;

    public Gateway getGateway() throws Exception {

        if (gateway != null) {
            return gateway;
        }

        channel = newGrpcConnection();

        gateway = Gateway.newInstance()
                .identity(fabricIdentityService.getIdentity())
                .signer(fabricIdentityService.getSigner())
                .connection(channel)
                .connect();

        return gateway;
    }

    private ManagedChannel newGrpcConnection() throws IOException {

        var tlsCert = Files.newInputStream(
                Path.of(fabricProperties.getTlsCertPath())
        );

        var tlsCredentials = TlsChannelCredentials.newBuilder()
                .trustManager(tlsCert)
                .build();

        return NettyChannelBuilder
                .forTarget(fabricProperties.getPeerEndpoint(), tlsCredentials)
                .overrideAuthority(fabricProperties.getPeerHostAlias())
                .build();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {

        if (gateway != null) {
            gateway.close();
        }

        if (channel != null) {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}