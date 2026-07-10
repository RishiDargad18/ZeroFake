package util

import "time"

// CurrentTimestamp returns the current UTC timestamp
// formatted using RFC3339.
func CurrentTimestamp() string {
	return time.Now().UTC().Format(time.RFC3339)
}