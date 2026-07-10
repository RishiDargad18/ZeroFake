package contract

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"
	"github.com/zerofake/zerofake-chaincode/constant"
	"github.com/zerofake/zerofake-chaincode/model"
	"github.com/zerofake/zerofake-chaincode/util"
)

// ProductContract defines the ZeroFake smart contract.
//
// It exposes the public blockchain transactions responsible for
// managing product registration, ownership transfer, product
// verification, and retrieval of product history.
//
// Transaction implementations are added in subsequent milestones.
type ProductContract struct {
	contractapi.Contract
}

// RegisterProduct registers a new product on the blockchain.
func (p *ProductContract) RegisterProduct(
	ctx contractapi.TransactionContextInterface,
	productID string,
	manufacturerID string,
) (*model.ProductAsset, error) {

	if err := util.ValidateProductID(productID); err != nil {
		return nil, err
	}

	if err := util.ValidateOwnerID(manufacturerID); err != nil {
		return nil, err
	}

	existingProduct, err := ctx.GetStub().GetState(productID)
	if err != nil {
		return nil, fmt.Errorf("failed to check product existence: %w", err)
	}

	if existingProduct != nil {
		return nil, fmt.Errorf("product with ID '%s' already exists", productID)
	}

	currentTimestamp := util.CurrentTimestamp()

	product := &model.ProductAsset{
		ProductID:        productID,
		ManufacturerID:   manufacturerID,
		CurrentOwnerID:   manufacturerID,
		CurrentOwnerRole: constant.OwnerRoleManufacturer,
		ProductStatus:    constant.ProductStatusRegistered,
		IsVerified:       true,
		CreatedAt:        currentTimestamp,
		UpdatedAt:        currentTimestamp,
	}

	productJSON, err := json.Marshal(product)
	if err != nil {
		return nil, fmt.Errorf("failed to serialize product: %w", err)
	}

	if err := ctx.GetStub().PutState(productID, productJSON); err != nil {
		return nil, fmt.Errorf("failed to register product: %w", err)
	}

	return product, nil
}

// productExists checks whether a product exists on the ledger.
func (p *ProductContract) productExists(
	ctx contractapi.TransactionContextInterface,
	productID string,
) (bool, error) {

	productBytes, err := ctx.GetStub().GetState(productID)
	if err != nil {
		return false, fmt.Errorf("failed to retrieve product '%s': %w", productID, err)
	}

	return productBytes != nil, nil
}

// readProduct retrieves and deserializes a product from the ledger.
func (p *ProductContract) readProduct(
	ctx contractapi.TransactionContextInterface,
	productID string,
) (*model.ProductAsset, error) {

	productBytes, err := ctx.GetStub().GetState(productID)
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve product '%s': %w", productID, err)
	}

	if productBytes == nil {
		return nil, fmt.Errorf("product with ID '%s' does not exist", productID)
	}

	var product model.ProductAsset
	if err := json.Unmarshal(productBytes, &product); err != nil {
		return nil, fmt.Errorf("failed to deserialize product '%s': %w", productID, err)
	}

	return &product, nil
}

// TransferOwnership transfers ownership of a product to a new owner.
func (p *ProductContract) TransferOwnership(
	ctx contractapi.TransactionContextInterface,
	productID string,
	currentOwnerID string,
	newOwnerID string,
	newOwnerRole string,
) (*model.ProductAsset, error) {

	if err := util.ValidateProductID(productID); err != nil {
		return nil, err
	}

	if err := util.ValidateOwnerID(currentOwnerID); err != nil {
		return nil, err
	}

	if err := util.ValidateOwnerID(newOwnerID); err != nil {
		return nil, err
	}

	product, err := p.readProduct(ctx, productID)
	if err != nil {
		return nil, err
	}

	if product.CurrentOwnerID != currentOwnerID {
		return nil, fmt.Errorf("current owner '%s' does not own product '%s'", currentOwnerID, productID)
	}

	if currentOwnerID == newOwnerID {
		return nil, fmt.Errorf("new owner must be different from the current owner")
	}

	role := constant.OwnerRole(newOwnerRole)

	switch role {
	case constant.OwnerRoleManufacturer,
		constant.OwnerRoleWarehouse,
		constant.OwnerRoleDistributor,
		constant.OwnerRoleRetailer,
		constant.OwnerRoleCustomer:
		// Valid role
	default:
		return nil, fmt.Errorf("invalid owner role: %s", newOwnerRole)
	}

	product.CurrentOwnerID = newOwnerID
	product.CurrentOwnerRole = role
	product.UpdatedAt = util.CurrentTimestamp()

	switch role {
	case constant.OwnerRoleManufacturer:
		product.ProductStatus = constant.ProductStatusRegistered
	case constant.OwnerRoleWarehouse,
		constant.OwnerRoleDistributor,
		constant.OwnerRoleRetailer:
		product.ProductStatus = constant.ProductStatusInTransit
	case constant.OwnerRoleCustomer:
		product.ProductStatus = constant.ProductStatusDelivered
	}

	productJSON, err := json.Marshal(product)
	if err != nil {
		return nil, fmt.Errorf("failed to serialize product: %w", err)
	}

	if err := ctx.GetStub().PutState(productID, productJSON); err != nil {
		return nil, fmt.Errorf("failed to transfer ownership: %w", err)
	}

	return product, nil
}

// VerifyProduct verifies that a product exists on the ledger and is marked
// as verified. It does not modify the ledger state.
func (p *ProductContract) VerifyProduct(
	ctx contractapi.TransactionContextInterface,
	productID string,
) (*model.ProductAsset, error) {

	if err := util.ValidateProductID(productID); err != nil {
		return nil, err
	}

	product, err := p.readProduct(ctx, productID)
	if err != nil {
		return nil, err
	}

	if !product.IsVerified {
		return nil, fmt.Errorf("product with ID '%s' is not verified", productID)
	}

	return product, nil
}

// GetProductHistory retrieves the complete immutable history of a product
// from the Hyperledger Fabric ledger.
func (p *ProductContract) GetProductHistory(
	ctx contractapi.TransactionContextInterface,
	productID string,
) ([]*model.ProductAsset, error) {

	if err := util.ValidateProductID(productID); err != nil {
		return nil, err
	}

	// Verify that the product exists.
	if _, err := p.readProduct(ctx, productID); err != nil {
		return nil, err
	}

	historyIterator, err := ctx.GetStub().GetHistoryForKey(productID)
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve history for product '%s': %w", productID, err)
	}
	defer historyIterator.Close()

	var history []*model.ProductAsset

	for historyIterator.HasNext() {
		response, err := historyIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to iterate product history: %w", err)
		}

		// Skip delete records or empty values.
		if len(response.Value) == 0 {
			continue
		}

		var product model.ProductAsset
		if err := json.Unmarshal(response.Value, &product); err != nil {
			return nil, fmt.Errorf("failed to deserialize historical product record: %w", err)
		}

		history = append(history, &product)
	}

	return history, nil
}
