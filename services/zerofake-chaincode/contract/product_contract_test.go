package contract

import (
	"encoding/json"
	"errors"
	"strings"
	"testing"

	"github.com/zerofake/zerofake-chaincode/constant"
	"github.com/zerofake/zerofake-chaincode/model"
)

// These tests cover the rules that only the chaincode can enforce.
//
// Validation in the Java services is a convenience; validation here is the
// guarantee, because every endorsing peer re-executes this code and must agree
// before a transaction can commit. A compromised application server cannot
// write an ownership transfer that this file rejects. That makes these the
// highest-value tests in the project.

const (
	productID   = "3f2a91c4-0000-0000-0000-000000000001"
	manufacturer = "11111111-0000-0000-0000-000000000001"
	warehouse   = "22222222-0000-0000-0000-000000000002"
	distributor = "33333333-0000-0000-0000-000000000003"
	customer    = "44444444-0000-0000-0000-000000000004"
	stranger    = "99999999-0000-0000-0000-000000000009"
)

func seedProduct(t *testing.T, ctx *fakeContext) *model.ProductAsset {
	t.Helper()

	contract := &ProductContract{}

	asset, err := contract.RegisterProduct(ctx, productID, manufacturer)
	if err != nil {
		t.Fatalf("seeding failed: %v", err)
	}

	return asset
}

func readStored(t *testing.T, ctx *fakeContext, id string) *model.ProductAsset {
	t.Helper()

	raw, ok := ctx.stub.state[id]
	if !ok {
		t.Fatalf("no asset stored under %q", id)
	}

	var asset model.ProductAsset
	if err := json.Unmarshal(raw, &asset); err != nil {
		t.Fatalf("stored asset is not valid JSON: %v", err)
	}

	return &asset
}

// ---------------------------------------------------------------------------
// RegisterProduct
// ---------------------------------------------------------------------------

func TestRegisterProduct_CreatesAssetOwnedByItsManufacturer(t *testing.T) {
	ctx := newFakeContext()

	asset := seedProduct(t, ctx)

	if asset.ProductID != productID {
		t.Errorf("ProductID = %q, want %q", asset.ProductID, productID)
	}
	if asset.ManufacturerID != manufacturer {
		t.Errorf("ManufacturerID = %q, want %q", asset.ManufacturerID, manufacturer)
	}
	// The manufacturer holds custody until it hands the goods on.
	if asset.CurrentOwnerID != manufacturer {
		t.Errorf("CurrentOwnerID = %q, want the manufacturer %q", asset.CurrentOwnerID, manufacturer)
	}
	if asset.CurrentOwnerRole != constant.OwnerRoleManufacturer {
		t.Errorf("CurrentOwnerRole = %q, want MANUFACTURER", asset.CurrentOwnerRole)
	}
	if asset.ProductStatus != constant.ProductStatusRegistered {
		t.Errorf("ProductStatus = %q, want REGISTERED", asset.ProductStatus)
	}
	if !asset.IsVerified {
		t.Error("IsVerified = false, want true on registration")
	}
}

func TestRegisterProduct_PersistsToTheLedger(t *testing.T) {
	ctx := newFakeContext()

	seedProduct(t, ctx)

	stored := readStored(t, ctx, productID)
	if stored.ProductID != productID {
		t.Errorf("stored ProductID = %q, want %q", stored.ProductID, productID)
	}
}

func TestRegisterProduct_UsesTheTransactionTimestamp(t *testing.T) {
	ctx := newFakeContext()

	asset := seedProduct(t, ctx)

	// Chaincode must be deterministic: every endorsing peer executes it and
	// their write sets have to match byte for byte. A wall-clock read would
	// differ between peers and endorsement would fail. The timestamp therefore
	// comes from the transaction, and this asserts the value the fake supplied
	// rather than anything derived from the current time.
	const want = "2026-03-14T09:26:53Z"

	if asset.CreatedAt != want {
		t.Errorf("CreatedAt = %q, want the transaction timestamp %q", asset.CreatedAt, want)
	}
	if asset.UpdatedAt != want {
		t.Errorf("UpdatedAt = %q, want the transaction timestamp %q", asset.UpdatedAt, want)
	}
}

func TestRegisterProduct_RejectsDuplicateRegistration(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	_, err := contract.RegisterProduct(ctx, productID, manufacturer)
	if err == nil {
		t.Fatal("expected an error registering the same product twice")
	}

	// The blockchain service keys on this phrase to turn a duplicate into a
	// 409 rather than a 502, so the wording is a cross-component contract.
	if !strings.Contains(err.Error(), "already exists") {
		t.Errorf("error = %q, want it to contain \"already exists\"", err)
	}
}

func TestRegisterProduct_RejectsBlankIdentifiers(t *testing.T) {
	contract := &ProductContract{}

	cases := []struct {
		name           string
		productID      string
		manufacturerID string
	}{
		{"empty product id", "", manufacturer},
		{"whitespace product id", "   ", manufacturer},
		{"empty manufacturer id", productID, ""},
		{"whitespace manufacturer id", productID, "  "},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			ctx := newFakeContext()

			if _, err := contract.RegisterProduct(ctx, tc.productID, tc.manufacturerID); err == nil {
				t.Fatal("expected an error, got none")
			}
			if len(ctx.stub.state) != 0 {
				t.Error("a rejected registration must not write to the ledger")
			}
		})
	}
}

func TestRegisterProduct_PropagatesLedgerReadFailure(t *testing.T) {
	ctx := newFakeContext()
	ctx.stub.getStateErr = errors.New("ledger unavailable")

	contract := &ProductContract{}

	if _, err := contract.RegisterProduct(ctx, productID, manufacturer); err == nil {
		t.Fatal("expected the ledger failure to surface, got nil")
	}
}

// ---------------------------------------------------------------------------
// TransferOwnership
// ---------------------------------------------------------------------------

func TestTransferOwnership_MovesCustodyToTheNewOwner(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	asset, err := contract.TransferOwnership(ctx, productID, manufacturer, warehouse, "WAREHOUSE")
	if err != nil {
		t.Fatalf("transfer failed: %v", err)
	}

	if asset.CurrentOwnerID != warehouse {
		t.Errorf("CurrentOwnerID = %q, want %q", asset.CurrentOwnerID, warehouse)
	}
	if asset.CurrentOwnerRole != constant.OwnerRoleWarehouse {
		t.Errorf("CurrentOwnerRole = %q, want WAREHOUSE", asset.CurrentOwnerRole)
	}

	stored := readStored(t, ctx, productID)
	if stored.CurrentOwnerID != warehouse {
		t.Errorf("the transfer was not persisted: stored owner = %q", stored.CurrentOwnerID)
	}
}

// The single most important rule in the contract. Everything the platform
// claims about custody rests on a peer refusing this.
func TestTransferOwnership_RejectsTransferByNonOwner(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	_, err := contract.TransferOwnership(ctx, productID, stranger, customer, "CUSTOMER")
	if err == nil {
		t.Fatal("a party that does not hold the product must not be able to transfer it")
	}
	if !strings.Contains(err.Error(), "does not own") {
		t.Errorf("error = %q, want it to explain the ownership failure", err)
	}

	stored := readStored(t, ctx, productID)
	if stored.CurrentOwnerID != manufacturer {
		t.Errorf("a rejected transfer changed the owner to %q", stored.CurrentOwnerID)
	}
}

func TestTransferOwnership_RejectsTransferToSelf(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	if _, err := contract.TransferOwnership(ctx, productID, manufacturer, manufacturer, "MANUFACTURER"); err == nil {
		t.Fatal("expected an error transferring to the current owner")
	}
}

func TestTransferOwnership_RejectsUnknownProduct(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	_, err := contract.TransferOwnership(ctx, "no-such-product", manufacturer, warehouse, "WAREHOUSE")
	if err == nil {
		t.Fatal("expected an error for a product that is not on the ledger")
	}
	if !strings.Contains(err.Error(), "does not exist") {
		t.Errorf("error = %q, want it to contain \"does not exist\"", err)
	}
}

func TestTransferOwnership_RejectsInvalidRole(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	for _, role := range []string{"", "ADMIN", "warehouse", "SUPPLIER"} {
		t.Run("role="+role, func(t *testing.T) {
			if _, err := contract.TransferOwnership(ctx, productID, manufacturer, warehouse, role); err == nil {
				t.Fatalf("role %q was accepted; only the five supply chain roles are valid", role)
			}
		})
	}
}

func TestTransferOwnership_DerivesStatusFromTheNewRole(t *testing.T) {
	contract := &ProductContract{}

	cases := []struct {
		role       string
		wantStatus constant.ProductStatus
	}{
		{"WAREHOUSE", constant.ProductStatusInTransit},
		{"DISTRIBUTOR", constant.ProductStatusInTransit},
		{"RETAILER", constant.ProductStatusInTransit},
		// Reaching the customer is the end of the supply chain.
		{"CUSTOMER", constant.ProductStatusDelivered},
	}

	for _, tc := range cases {
		t.Run(tc.role, func(t *testing.T) {
			ctx := newFakeContext()
			seedProduct(t, ctx)

			asset, err := contract.TransferOwnership(ctx, productID, manufacturer, distributor, tc.role)
			if err != nil {
				t.Fatalf("transfer failed: %v", err)
			}
			if asset.ProductStatus != tc.wantStatus {
				t.Errorf("status after transfer to %s = %q, want %q",
					tc.role, asset.ProductStatus, tc.wantStatus)
			}
		})
	}
}

func TestTransferOwnership_PreservesCreationTimestamp(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	original := seedProduct(t, ctx)
	createdAt := original.CreatedAt

	// A later transaction carries a later timestamp.
	ctx.stub.txTimestamp = ctx.stub.txTimestamp.Add(48 * 3600 * 1e9)

	asset, err := contract.TransferOwnership(ctx, productID, manufacturer, warehouse, "WAREHOUSE")
	if err != nil {
		t.Fatalf("transfer failed: %v", err)
	}

	if asset.CreatedAt != createdAt {
		t.Errorf("CreatedAt changed on transfer: %q -> %q", createdAt, asset.CreatedAt)
	}
	if asset.UpdatedAt == createdAt {
		t.Error("UpdatedAt did not advance on transfer")
	}
}

func TestTransferOwnership_SupportsAFullSupplyChain(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	hops := []struct {
		from, to, role string
	}{
		{manufacturer, warehouse, "WAREHOUSE"},
		{warehouse, distributor, "DISTRIBUTOR"},
		{distributor, customer, "CUSTOMER"},
	}

	for _, hop := range hops {
		if _, err := contract.TransferOwnership(ctx, productID, hop.from, hop.to, hop.role); err != nil {
			t.Fatalf("transfer %s -> %s failed: %v", hop.from, hop.to, err)
		}
	}

	final := readStored(t, ctx, productID)
	if final.CurrentOwnerID != customer {
		t.Errorf("final owner = %q, want the customer %q", final.CurrentOwnerID, customer)
	}
	if final.ProductStatus != constant.ProductStatusDelivered {
		t.Errorf("final status = %q, want DELIVERED", final.ProductStatus)
	}
	// The manufacturer is a permanent fact about the product, not a role that
	// moves with custody.
	if final.ManufacturerID != manufacturer {
		t.Errorf("ManufacturerID changed to %q during transfers", final.ManufacturerID)
	}
}

// ---------------------------------------------------------------------------
// VerifyProduct
// ---------------------------------------------------------------------------

func TestVerifyProduct_ReturnsTheOnChainState(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	asset, err := contract.VerifyProduct(ctx, productID)
	if err != nil {
		t.Fatalf("verification failed: %v", err)
	}
	if asset.ProductID != productID {
		t.Errorf("ProductID = %q, want %q", asset.ProductID, productID)
	}
	if asset.CurrentOwnerID != manufacturer {
		t.Errorf("CurrentOwnerID = %q, want %q", asset.CurrentOwnerID, manufacturer)
	}
}

// This wording is load-bearing. The blockchain service matches on it to return
// 404, which the fraud service reads as BLOCKCHAIN_MISMATCH. If it changed,
// a counterfeit would surface as a 502 "cannot verify" instead of a verdict.
func TestVerifyProduct_UnknownProductSaysDoesNotExist(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	_, err := contract.VerifyProduct(ctx, "no-such-product")
	if err == nil {
		t.Fatal("expected an error for a product that was never registered")
	}
	if !strings.Contains(err.Error(), "does not exist") {
		t.Errorf("error = %q, want it to contain \"does not exist\" "+
			"(the blockchain service matches on this to produce a 404)", err)
	}
}

func TestVerifyProduct_RejectsBlankProductID(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	if _, err := contract.VerifyProduct(ctx, "  "); err == nil {
		t.Fatal("expected an error for a blank product id")
	}
}

func TestVerifyProduct_DoesNotWriteToTheLedger(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)
	before := len(ctx.stub.history[productID])

	if _, err := contract.VerifyProduct(ctx, productID); err != nil {
		t.Fatalf("verification failed: %v", err)
	}

	// Verification is a query. If it wrote, it would consume block space and
	// mutate the very history the fraud rules reason about.
	if after := len(ctx.stub.history[productID]); after != before {
		t.Errorf("verification wrote to the ledger: %d writes before, %d after", before, after)
	}
}

// ---------------------------------------------------------------------------
// GetProductHistory
// ---------------------------------------------------------------------------

func TestGetProductHistory_ReturnsEveryVersionOldestFirst(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)

	if _, err := contract.TransferOwnership(ctx, productID, manufacturer, warehouse, "WAREHOUSE"); err != nil {
		t.Fatalf("transfer failed: %v", err)
	}
	if _, err := contract.TransferOwnership(ctx, productID, warehouse, customer, "CUSTOMER"); err != nil {
		t.Fatalf("transfer failed: %v", err)
	}

	history, err := contract.GetProductHistory(ctx, productID)
	if err != nil {
		t.Fatalf("history failed: %v", err)
	}

	if len(history) != 3 {
		t.Fatalf("history has %d entries, want 3 (registration plus two transfers)", len(history))
	}

	// Fabric returns history oldest first, and the blockchain service relies on
	// that ordering when it reads the current owner from the last entry.
	wantOwners := []string{manufacturer, warehouse, customer}
	for i, want := range wantOwners {
		if history[i].CurrentOwnerID != want {
			t.Errorf("history[%d].CurrentOwnerID = %q, want %q", i, history[i].CurrentOwnerID, want)
		}
	}
}

func TestGetProductHistory_SkipsDeletionRecords(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)
	ctx.stub.appendDeletion(productID)

	history, err := contract.GetProductHistory(ctx, productID)
	if err != nil {
		t.Fatalf("history failed: %v", err)
	}

	// A tombstone has no value to deserialise; including it would corrupt the
	// trail with an empty asset.
	if len(history) != 1 {
		t.Fatalf("history has %d entries, want 1 with the deletion skipped", len(history))
	}
}

func TestGetProductHistory_RejectsUnknownProduct(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	_, err := contract.GetProductHistory(ctx, "no-such-product")
	if err == nil {
		t.Fatal("expected an error for a product that is not on the ledger")
	}
	if !strings.Contains(err.Error(), "does not exist") {
		t.Errorf("error = %q, want it to contain \"does not exist\"", err)
	}
}

func TestGetProductHistory_PropagatesIteratorFailure(t *testing.T) {
	ctx := newFakeContext()
	contract := &ProductContract{}

	seedProduct(t, ctx)
	ctx.stub.historyErr = errors.New("history unavailable")

	if _, err := contract.GetProductHistory(ctx, productID); err == nil {
		t.Fatal("expected the iterator failure to surface, got nil")
	}
}

// ---------------------------------------------------------------------------
// Serialisation
// ---------------------------------------------------------------------------

// The Java side reads these names off the wire, so they are part of the
// contract between the chaincode and the blockchain service.
func TestProductAsset_JSONFieldNames(t *testing.T) {
	ctx := newFakeContext()

	seedProduct(t, ctx)

	var decoded map[string]any
	if err := json.Unmarshal(ctx.stub.state[productID], &decoded); err != nil {
		t.Fatalf("stored asset is not valid JSON: %v", err)
	}

	for _, field := range []string{
		"productId", "manufacturerId", "currentOwnerId",
		"currentOwnerRole", "productStatus", "isVerified",
		"createdAt", "updatedAt",
	} {
		if _, ok := decoded[field]; !ok {
			t.Errorf("serialised asset is missing %q", field)
		}
	}
}
