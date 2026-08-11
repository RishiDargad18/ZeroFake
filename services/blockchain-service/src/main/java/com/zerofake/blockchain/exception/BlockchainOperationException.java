package com.zerofake.blockchain.exception;

/**
 * Raised when a ledger operation could not be completed — the peer was
 * unreachable, endorsement was refused, or the transaction failed to commit.
 *
 * <p>Mapped to 502 Bad Gateway: the request itself was well formed, but the
 * blockchain network could not service it.
 */
public class BlockchainOperationException extends RuntimeException {

    public BlockchainOperationException(String message) {
        super(message);
    }

    public BlockchainOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
