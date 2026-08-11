package com.zerofake.auth.constant;

/**
 * Names of the custom claims carried by ZeroFake access tokens.
 *
 * <p>These claim names form part of the contract between the authentication
 * service and every service that validates its tokens. They must be kept in
 * sync with the corresponding constants in the product, blockchain and fraud
 * detection services.
 */
public final class JwtClaims {

    public static final String USER_ID = "userId";

    public static final String ROLE = "role";

    private JwtClaims() {
    }
}
