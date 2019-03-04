// +build !android

package utils

import (
	"syscall"
)

var VpnMode bool

func ControlOnConnSetup(network string, address string, c syscall.RawConn) error {
	return nil
}
