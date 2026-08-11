package com.zerofake.blockchain.fabric;

import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.protos.gateway.ErrorDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the message the chaincode actually returned from a Fabric client
 * exception, so that a business-level rejection ("product does not exist") can
 * be told apart from an infrastructure failure ("peer unreachable").
 *
 * <p>A chaincode's own message is carried in the gRPC error details rather than
 * in the exception message, so the details are read directly. This is
 * deliberately narrow: it inspects the structured error and the exception's
 * cause chain, never a rendered stack trace.
 */
public final class ChaincodeErrors {

    private static final String NOT_FOUND_MARKER = "does not exist";
    private static final String ALREADY_EXISTS_MARKER = "already exists";

    /** Guards against a pathological or cyclic cause chain. */
    private static final int MAX_CAUSE_DEPTH = 5;

    private ChaincodeErrors() {
    }

    /**
     * Returns the chaincode-supplied messages carried by the exception, together
     * with the messages of the exception and its causes.
     */
    public static List<String> messagesOf(Throwable throwable) {

        List<String> messages = new ArrayList<>();

        Throwable current = throwable;

        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {

            if (current.getMessage() != null) {
                messages.add(current.getMessage());
            }

            if (current instanceof GatewayException gatewayException) {
                for (ErrorDetail detail : gatewayException.getDetails()) {
                    messages.add(detail.getMessage());
                }
            }

            current = current.getCause();
        }

        return messages;
    }

    /** Whether the chaincode rejected the call because the product is not on the ledger. */
    public static boolean isNotFound(Throwable throwable) {
        return containsMarker(throwable, NOT_FOUND_MARKER);
    }

    /** Whether the chaincode rejected the call because the product is already on the ledger. */
    public static boolean isAlreadyExists(Throwable throwable) {
        return containsMarker(throwable, ALREADY_EXISTS_MARKER);
    }

    /**
     * Returns the most descriptive message available, for logging and for the
     * {@code message} column of the local audit record.
     *
     * <p>Prefers a chaincode detail message when one is present, since that is
     * the ledger's own explanation of the rejection.
     */
    public static String describe(Throwable throwable) {

        return messagesOf(throwable).stream()
                .filter(message -> message != null && !message.isBlank())
                .reduce((first, second) -> second)
                .orElse(throwable.getClass().getSimpleName());
    }

    private static boolean containsMarker(Throwable throwable, String marker) {

        return messagesOf(throwable).stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(message -> message.toLowerCase().contains(marker));
    }
}
