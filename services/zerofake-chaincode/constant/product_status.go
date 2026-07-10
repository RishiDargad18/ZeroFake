package constant

// ProductStatus represents the current lifecycle state of a product
// on the blockchain.
type ProductStatus string

const (
	ProductStatusRegistered ProductStatus = "REGISTERED"
	ProductStatusInTransit  ProductStatus = "IN_TRANSIT"
	ProductStatusDelivered  ProductStatus = "DELIVERED"
)