package com.zerofake.blockchain.fabric;

import com.zerofake.blockchain.config.FabricProperties;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FabricIdentityService {

    private final FabricProperties fabricProperties;

    public Identity getIdentity() throws IOException, CertificateException {

        X509Certificate certificate = Identities.readX509Certificate(
                Files.newBufferedReader(Path.of(fabricProperties.getCertPath()))
        );

        return new X509Identity(
                fabricProperties.getMspId(),
                certificate
        );
    }

    public Signer getSigner() throws IOException, InvalidKeyException {

        Path keyDirectory = Path.of(fabricProperties.getKeyDirectoryPath());

        Path privateKeyPath;

        try (Stream<Path> keyFiles = Files.list(keyDirectory)) {

            privateKeyPath = keyFiles
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("No private key found."));
        }

        PrivateKey privateKey = Identities.readPrivateKey(
                Files.newBufferedReader(privateKeyPath)
        );

        return Signers.newPrivateKeySigner(privateKey);
    }
}