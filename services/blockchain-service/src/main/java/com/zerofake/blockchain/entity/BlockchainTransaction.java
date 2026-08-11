package com.zerofake.blockchain.entity;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * The local audit record of a ledger transaction.
 *
 * <p>Every field that describes the on-chain transaction is taken directly from
 * Hyperledger Fabric. Nothing here is ever synthesised: if a value could not be
 * obtained from the ledger it is left null rather than invented, because a
 * fabricated transaction identifier would make this audit trail worthless.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "blockchain_transactions",
        indexes = {
                @Index(name = "idx_blockchain_tx_product", columnList = "productId"),
                @Index(name = "idx_blockchain_tx_transaction_id", columnList = "transactionId")
        }
)
public class BlockchainTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    /**
     * The Fabric transaction identifier.
     *
     * <p>Nullable because a proposal that fails before it is assigned an
     * identifier still needs to be recorded as a FAILED attempt.
     */
    @Column(unique = true, length = 255)
    private String transactionId;

    /** The ledger block this transaction was committed in, once known. */
    @Column
    private Long blockNumber;

    /**
     * Reserved for the committing block's hash.
     *
     * <p>The Fabric Gateway client does not expose the block hash for a
     * submitted transaction; obtaining it requires a block event listener.
     * The column is retained for that future capability and stays null until then.
     */
    @Column(length = 255)
    private String blockHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransactionType transactionType;

    @Column(nullable = false)
    private UUID performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BlockchainStatus status = BlockchainStatus.PENDING;

    @Column(length = 500)
    private String message;

}
