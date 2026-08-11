package com.zerofake.blockchain.fabric;

import com.zerofake.blockchain.config.FabricProperties;
import com.zerofake.blockchain.exception.BlockchainOperationException;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Owns the single gRPC channel and gateway connection to the Fabric peer.
 *
 * <p>The connection is established lazily on first use and guarded so that
 * concurrent first requests cannot each open a channel — every channel but the
 * last would then leak past shutdown.
 */
@Service
@RequiredArgsConstructor
public class FabricGatewayService {

    private static final Logger log = LoggerFactory.getLogger(FabricGatewayService.class);

    private final FabricProperties fabricProperties;
    private final FabricIdentityService fabricIdentityService;

    private final Object lock = new Object();

    private volatile Gateway gateway;
    private ManagedChannel channel;

    public Gateway getGateway() {

        Gateway current = gateway;

        if (current != null) {
            return current;
        }

        synchronized (lock) {

            if (gateway != null) {
                return gateway;
            }

            try {
                channel = newGrpcConnection();

                gateway = Gateway.newInstance()
                        .identity(fabricIdentityService.getIdentity())
                        .signer(fabricIdentityService.getSigner())
                        .connection(channel)
                        .endorseOptions(options -> options.withDeadlineAfter(30, TimeUnit.SECONDS))
                        .submitOptions(options -> options.withDeadlineAfter(30, TimeUnit.SECONDS))
                        .commitStatusOptions(options -> options.withDeadlineAfter(60, TimeUnit.SECONDS))
                        .evaluateOptions(options -> options.withDeadlineAfter(30, TimeUnit.SECONDS))
                        .connect();

                log.info(
                        "Connected to Fabric peer {} on channel {}",
                        fabricProperties.getPeerEndpoint(),
                        fabricProperties.getChannelName()
                );

                return gateway;

            } catch (Exception ex) {
                closeQuietly();

                throw new BlockchainOperationException(
                        "Unable to connect to the Hyperledger Fabric network. "
                                + "Check that the peer is running and that the Fabric "
                                + "credentials are configured correctly.",
                        ex
                );
            }
        }
    }

    private ManagedChannel newGrpcConnection() throws IOException {

        try (InputStream tlsCert = Files.newInputStream(
                Path.of(fabricProperties.getTlsCertPath()))) {

            var tlsCredentials = TlsChannelCredentials.newBuilder()
                    .trustManager(tlsCert)
                    .build();

            return NettyChannelBuilder
                    .forTarget(fabricProperties.getPeerEndpoint(), tlsCredentials)
                    .overrideAuthority(fabricProperties.getPeerHostAlias())
                    .build();
        }
    }

    @PreDestroy
    public void shutdown() {
        synchronized (lock) {
            closeQuietly();
        }
    }

    private void closeQuietly() {

        if (gateway != null) {
            try {
                gateway.close();
            } catch (Exception ex) {
                log.warn("Failed to close Fabric gateway cleanly: {}", ex.getMessage());
            }
            gateway = null;
        }

        if (channel != null) {
            try {
                channel.shutdownNow();
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.warn("Failed to close Fabric gRPC channel cleanly: {}", ex.getMessage());
            }
            channel = null;
        }
    }
}
