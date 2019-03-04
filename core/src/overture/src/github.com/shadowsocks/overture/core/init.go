// Copyright (c) 2016 shawn1m. All rights reserved.
// Use of this source code is governed by The MIT License (MIT) that can be
// found in the LICENSE file.

// Package core implements the essential features.
package core

import (
	"github.com/shadowsocks/overture/core/config"
	"github.com/shadowsocks/overture/core/inbound"
	"github.com/shadowsocks/overture/core/outbound"
	"github.com/shadowsocks/overture/core/utils"
)

// Initiate the server with config file
func InitServer(configFilePath string, vpnMode bool) {

	config := config.NewConfig(configFilePath)

	// New dispatcher without ClientBundle, ClientBundle must be initiated when server is running
	d := outbound.Dispatcher{
		PrimaryDNS:         config.PrimaryDNS,
		AlternativeDNS:     config.AlternativeDNS,
		OnlyPrimaryDNS:     config.OnlyPrimaryDNS,
		IPNetworkList:      config.IPNetworkList,
		AclList:            config.AclList,
		RedirectIPv6Record: config.RedirectIPv6Record,
		Hosts:              config.Hosts,
		Cache:              config.Cache,
	}

	s := &inbound.Server{
		BindAddress: config.BindAddress,
		Dispatcher:  d,
		MinimumTTL:  config.MinimumTTL,
		RejectQtype: config.RejectQtype,
	}

	utils.VpnMode = vpnMode

	s.Run()
}
