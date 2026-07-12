package util

import (
	"time"

	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"
)

func CurrentTimestamp(ctx contractapi.TransactionContextInterface) (string, error) {

	txTime, err := ctx.GetStub().GetTxTimestamp()
	if err != nil {
		return "", err
	}

	timestamp := time.Unix(txTime.Seconds, int64(txTime.Nanos)).UTC()

	return timestamp.Format(time.RFC3339), nil
}