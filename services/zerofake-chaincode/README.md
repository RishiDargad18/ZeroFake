# ZeroFake Chaincode

## Overview

ZeroFake is a Blockchain-Based Fake Product Detection & Supply Chain Verification Platform built using Hyperledger Fabric and Spring Boot microservices.

This chaincode is responsible for maintaining the immutable blockchain state of products throughout the supply chain.

The chaincode does not handle authentication, user management, databases, or REST APIs. It only manages blockchain state and exposes transactions required by the Blockchain Service.

---

## Ledger Model

The ledger contains a single asset type:

- **ProductAsset**

Each ProductAsset represents the current blockchain state of a product.

Historical ownership records are retrieved using Hyperledger Fabric's history APIs rather than storing duplicate history inside the asset.

---

## Public Transactions

The chaincode exposes the following public transactions:

- `registerProduct`
- `transferOwnership`
- `verifyProduct`
- `getProductHistory`

These transactions support product registration, ownership transfer, authenticity verification, and retrieval of immutable ownership history.

---

## Technology

- Go
- Hyperledger Fabric 2.5 LTS
- Hyperledger Fabric Contract API v2