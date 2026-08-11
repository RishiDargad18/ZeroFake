package com.zerofake.blockchain.fabric;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hyperledger.fabric.client.EndorseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Classification of Hyperledger Fabric errors.
 *
 * <p>Getting this wrong has real consequences: a chaincode saying "this product
 * does not exist" must become a 404 that the fraud service reads as a
 * counterfeit signal, while a peer being unreachable must become a 502 that
 * says "we could not check". Conflating the two is the worst failure mode an
 * anti-counterfeiting system has.
 *
 * <p>The previous implementation determined this by searching a rendered stack
 * trace for a substring. These tests pin the replacement to the structured
 * error instead.
 */
class ChaincodeErrorsTest {

    private static StatusRuntimeException grpcError(String description) {
        return new StatusRuntimeException(Status.ABORTED.withDescription(description));
    }

    @Nested
    @DisplayName("not-found detection")
    class NotFoundDetection {

        @Test
        @DisplayName("recognises the chaincode's not-found rejection")
        void recognisesNotFoundRejection() {

            Exception ex = new RuntimeException(
                    "product with ID 'abc' does not exist"
            );

            assertThat(ChaincodeErrors.isNotFound(ex)).isTrue();
        }

        @Test
        @DisplayName("recognises it through a cause chain")
        void recognisesItThroughCauseChain() {

            Exception ex = new RuntimeException(
                    "evaluate failed",
                    new IllegalStateException(
                            "chaincode response",
                            new RuntimeException("product with ID 'abc' does not exist")
                    )
            );

            assertThat(ChaincodeErrors.isNotFound(ex)).isTrue();
        }

        @Test
        @DisplayName("recognises it inside a gateway exception")
        void recognisesItInsideGatewayException() {

            EndorseException ex = new EndorseException(
                    "tx-1",
                    grpcError("product with ID 'abc' does not exist")
            );

            assertThat(ChaincodeErrors.isNotFound(ex)).isTrue();
        }

        @Test
        @DisplayName("is case insensitive")
        void isCaseInsensitive() {

            assertThat(ChaincodeErrors.isNotFound(
                    new RuntimeException("Product DOES NOT EXIST on the ledger"))
            ).isTrue();
        }

        @Test
        @DisplayName("does not fire for an unreachable peer")
        void doesNotFireForUnreachablePeer() {

            // The distinction that matters: this must become a 502, not a
            // "counterfeit" verdict.
            Exception ex = new RuntimeException(
                    "UNAVAILABLE: io exception",
                    new IOException("Connection refused: localhost/127.0.0.1:7051")
            );

            assertThat(ChaincodeErrors.isNotFound(ex)).isFalse();
            assertThat(ChaincodeErrors.isAlreadyExists(ex)).isFalse();
        }

        @Test
        @DisplayName("does not fire for an exception with no message at all")
        void doesNotFireForMessagelessException() {

            assertThat(ChaincodeErrors.isNotFound(new RuntimeException())).isFalse();
        }
    }

    @Nested
    @DisplayName("already-exists detection")
    class AlreadyExistsDetection {

        @Test
        @DisplayName("recognises a duplicate registration")
        void recognisesDuplicateRegistration() {

            EndorseException ex = new EndorseException(
                    "tx-2",
                    grpcError("product with ID 'abc' already exists")
            );

            assertThat(ChaincodeErrors.isAlreadyExists(ex)).isTrue();
            assertThat(ChaincodeErrors.isNotFound(ex)).isFalse();
        }
    }

    @Nested
    @DisplayName("describe")
    class Describe {

        @Test
        @DisplayName("returns the innermost message, which is the ledger's own explanation")
        void returnsInnermostMessage() {

            Exception ex = new RuntimeException(
                    "submit failed",
                    new RuntimeException("current owner 'x' does not own product 'y'")
            );

            assertThat(ChaincodeErrors.describe(ex))
                    .isEqualTo("current owner 'x' does not own product 'y'");
        }

        @Test
        @DisplayName("falls back to the exception type when there is no message")
        void fallsBackToExceptionType() {

            assertThat(ChaincodeErrors.describe(new IllegalStateException()))
                    .isEqualTo("IllegalStateException");
        }

        @Test
        @DisplayName("never returns null")
        void neverReturnsNull() {

            assertThat(ChaincodeErrors.describe(new RuntimeException((String) null)))
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("cause chain traversal")
    class CauseChainTraversal {

        @Test
        @DisplayName("terminates on a self-referencing cause")
        void terminatesOnSelfReferencingCause() {

            // A cyclic chain must not hang the request thread.
            class Cyclic extends RuntimeException {
                Cyclic() {
                    super("cyclic");
                }

                @Override
                public synchronized Throwable getCause() {
                    return this;
                }
            }

            Cyclic ex = new Cyclic();

            assertThat(ChaincodeErrors.messagesOf(ex)).isNotEmpty();
            assertThat(ChaincodeErrors.isNotFound(ex)).isFalse();
        }

        @Test
        @DisplayName("collects messages from every level of the chain")
        void collectsMessagesFromEveryLevel() {

            Exception ex = new RuntimeException(
                    "outer",
                    new RuntimeException("middle", new RuntimeException("inner"))
            );

            assertThat(ChaincodeErrors.messagesOf(ex))
                    .containsExactly("outer", "middle", "inner");
        }

        @Test
        @DisplayName("stops before an unbounded chain exhausts the stack")
        void stopsBeforeUnboundedChainExhaustsStack() {

            RuntimeException deepest = new RuntimeException("level-0");
            RuntimeException current = deepest;

            for (int i = 1; i <= 50; i++) {
                current = new RuntimeException("level-" + i, current);
            }

            assertThat(ChaincodeErrors.messagesOf(current)).hasSizeLessThanOrEqualTo(10);
        }
    }
}
