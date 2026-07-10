package model

import "github.com/zerofake/zerofake-chaincode/constant"

// ProductAsset represents the current blockchain state of a product
// stored on the Hyperledger Fabric ledger.
//
// It contains the latest ownership, verification status, and lifecycle
// information for a product. Historical ownership records are retrieved
// using Hyperledger Fabric's history APIs rather than being stored
// within this asset.
type ProductAsset struct {
	ProductID        string                 `json:"productId"`
	ManufacturerID   string                 `json:"manufacturerId"`
	CurrentOwnerID   string                 `json:"currentOwnerId"`
	CurrentOwnerRole constant.OwnerRole     `json:"currentOwnerRole"`
	ProductStatus    constant.ProductStatus `json:"productStatus"`
	IsVerified       bool                   `json:"isVerified"`
	CreatedAt        string                 `json:"createdAt"`
	UpdatedAt        string                 `json:"updatedAt"`
}