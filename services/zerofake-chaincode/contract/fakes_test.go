package contract

import (
	"errors"
	"time"

	"github.com/hyperledger/fabric-chaincode-go/v2/shim"
	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"
	"github.com/hyperledger/fabric-protos-go-apiv2/ledger/queryresult"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// Test doubles for the Fabric chaincode stub.
//
// The real interfaces carry around thirty methods each, almost none of which
// this contract touches. Both fakes embed the interface they implement, so any
// method the contract calls but the fake does not define fails loudly with a
// nil-pointer panic rather than silently returning a zero value. That keeps the
// fakes small without letting an untested call slip through unnoticed.

// ---------------------------------------------------------------------------
// stub
// ---------------------------------------------------------------------------

type fakeStub struct {
	shim.ChaincodeStubInterface

	// state is the world state: the current value of every key.
	state map[string][]byte

	// history records every write in order, which is what GetHistoryForKey
	// replays. The real ledger keeps this because it is the chain itself.
	history map[string][]*queryresult.KeyModification

	txTimestamp time.Time

	// Failure injection, so error paths can be exercised.
	getStateErr   error
	putStateErr   error
	timestampErr  error
	historyErr    error
	historyIterErr error
}

func newFakeStub() *fakeStub {
	return &fakeStub{
		state:       make(map[string][]byte),
		history:     make(map[string][]*queryresult.KeyModification),
		txTimestamp: time.Date(2026, 3, 14, 9, 26, 53, 0, time.UTC),
	}
}

func (s *fakeStub) GetState(key string) ([]byte, error) {
	if s.getStateErr != nil {
		return nil, s.getStateErr
	}
	return s.state[key], nil
}

func (s *fakeStub) PutState(key string, value []byte) error {
	if s.putStateErr != nil {
		return s.putStateErr
	}

	stored := make([]byte, len(value))
	copy(stored, value)
	s.state[key] = stored

	s.history[key] = append(s.history[key], &queryresult.KeyModification{
		TxId:      "tx-" + key,
		Value:     stored,
		Timestamp: timestamppb.New(s.txTimestamp),
		IsDelete:  false,
	})

	return nil
}

func (s *fakeStub) GetTxTimestamp() (*timestamppb.Timestamp, error) {
	if s.timestampErr != nil {
		return nil, s.timestampErr
	}
	return timestamppb.New(s.txTimestamp), nil
}

func (s *fakeStub) GetHistoryForKey(key string) (shim.HistoryQueryIteratorInterface, error) {
	if s.historyErr != nil {
		return nil, s.historyErr
	}
	return &fakeHistoryIterator{
		entries: s.history[key],
		failAt:  -1,
		err:     s.historyIterErr,
	}, nil
}

// appendDeletion records a tombstone, which GetHistoryForKey must skip.
func (s *fakeStub) appendDeletion(key string) {
	s.history[key] = append(s.history[key], &queryresult.KeyModification{
		TxId:      "tx-delete-" + key,
		Value:     nil,
		Timestamp: timestamppb.New(s.txTimestamp),
		IsDelete:  true,
	})
}

// ---------------------------------------------------------------------------
// history iterator
// ---------------------------------------------------------------------------

type fakeHistoryIterator struct {
	entries []*queryresult.KeyModification
	index   int
	closed  bool

	// failAt makes Next return an error at a given index; -1 disables it.
	failAt int
	err    error
}

func (it *fakeHistoryIterator) HasNext() bool {
	return it.index < len(it.entries)
}

func (it *fakeHistoryIterator) Next() (*queryresult.KeyModification, error) {
	if it.err != nil && it.index == it.failAt {
		return nil, it.err
	}
	if it.index >= len(it.entries) {
		return nil, errors.New("no more entries")
	}

	entry := it.entries[it.index]
	it.index++

	return entry, nil
}

func (it *fakeHistoryIterator) Close() error {
	it.closed = true
	return nil
}

// ---------------------------------------------------------------------------
// transaction context
// ---------------------------------------------------------------------------

type fakeContext struct {
	contractapi.TransactionContextInterface

	stub *fakeStub
}

func newFakeContext() *fakeContext {
	return &fakeContext{stub: newFakeStub()}
}

func (c *fakeContext) GetStub() shim.ChaincodeStubInterface {
	return c.stub
}
