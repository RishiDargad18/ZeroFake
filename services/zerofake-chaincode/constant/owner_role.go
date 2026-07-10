package constant

// OwnerRole represents the current owner type in the supply chain.
type OwnerRole string

const (
	OwnerRoleManufacturer OwnerRole = "MANUFACTURER"
	OwnerRoleWarehouse    OwnerRole = "WAREHOUSE"
	OwnerRoleDistributor  OwnerRole = "DISTRIBUTOR"
	OwnerRoleRetailer     OwnerRole = "RETAILER"
	OwnerRoleCustomer     OwnerRole = "CUSTOMER"
)