package main

import (
	"log"

	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"

	"github.com/zerofake/zerofake-chaincode/contract"
)

func main() {
	chaincode, err := contractapi.NewChaincode(&contract.ProductContract{})
	if err != nil {
		log.Panicf("Error creating ZeroFake chaincode: %v", err)
	}

	if err := chaincode.Start(); err != nil {
		log.Panicf("Error starting ZeroFake chaincode: %v", err)
	}
}