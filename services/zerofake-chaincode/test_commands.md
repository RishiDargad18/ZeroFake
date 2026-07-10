# ZeroFake Chaincode Manual Test Commands

This document contains sample Hyperledger Fabric CLI commands to manually test the ZeroFake chaincode before integrating it with the Spring Boot Blockchain Service.

> **Note:** Replace `<CHAINCODE_NAME>` with the deployed chaincode name (for example, `zerofake`).

---

# Sample IDs

| Field | Value |
|-------|-------|
| Product ID | PRODUCT-1001 |
| Manufacturer ID | MANUFACTURER-001 |
| Warehouse ID | WAREHOUSE-001 |
| Distributor ID | DISTRIBUTOR-001 |
| Retailer ID | RETAILER-001 |
| Customer ID | CUSTOMER-001 |

---

# 1. Register Product

Register a new product on the blockchain.

```bash
peer chaincode invoke \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"RegisterProduct",
  "Args":[
    "PRODUCT-1001",
    "MANUFACTURER-001"
  ]
}'
```

---

# 2. Verify Product

Verify that the product exists and is marked as verified.

```bash
peer chaincode query \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"VerifyProduct",
  "Args":[
    "PRODUCT-1001"
  ]
}'
```

---

# 3. Transfer Ownership

## Manufacturer → Warehouse

```bash
peer chaincode invoke \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"TransferOwnership",
  "Args":[
    "PRODUCT-1001",
    "MANUFACTURER-001",
    "WAREHOUSE-001",
    "WAREHOUSE"
  ]
}'
```

---

## Warehouse → Distributor

```bash
peer chaincode invoke \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"TransferOwnership",
  "Args":[
    "PRODUCT-1001",
    "WAREHOUSE-001",
    "DISTRIBUTOR-001",
    "DISTRIBUTOR"
  ]
}'
```

---

## Distributor → Retailer

```bash
peer chaincode invoke \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"TransferOwnership",
  "Args":[
    "PRODUCT-1001",
    "DISTRIBUTOR-001",
    "RETAILER-001",
    "RETAILER"
  ]
}'
```

---

## Retailer → Customer

```bash
peer chaincode invoke \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"TransferOwnership",
  "Args":[
    "PRODUCT-1001",
    "RETAILER-001",
    "CUSTOMER-001",
    "CUSTOMER"
  ]
}'
```

---

# 4. Get Product History

Retrieve the immutable ownership history of the product.

```bash
peer chaincode query \
-C mychannel \
-n <CHAINCODE_NAME> \
-c '{
  "function":"GetProductHistory",
  "Args":[
    "PRODUCT-1001"
  ]
}'
```

---

# Recommended Execution Order

1. Register Product
2. Verify Product
3. Transfer Ownership (Manufacturer → Warehouse)
4. Transfer Ownership (Warehouse → Distributor)
5. Transfer Ownership (Distributor → Retailer)
6. Transfer Ownership (Retailer → Customer)
7. Verify Product
8. Get Product History