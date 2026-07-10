package util

import (
	"errors"
	"strings"
)

// ValidateProductID validates the supplied product ID.
func ValidateProductID(productID string) error {
	if strings.TrimSpace(productID) == "" {
		return errors.New("product ID must not be empty")
	}

	return nil
}

// ValidateOwnerID validates the supplied owner ID.
func ValidateOwnerID(ownerID string) error {
	if strings.TrimSpace(ownerID) == "" {
		return errors.New("owner ID must not be empty")
	}

	return nil
}