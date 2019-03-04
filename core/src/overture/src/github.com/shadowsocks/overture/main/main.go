// Copyright (c) 2016 holyshawn. All rights reserved.
// Use of this source code is governed by The MIT License (MIT) that can be
// found in the LICENSE file.

package main

import (
	"flag"
	"runtime"

	log "github.com/Sirupsen/logrus"
	"github.com/shadowsocks/overture/core"
)

func main() {

	var (
		configPath      string
		logPath         string
		isLogVerbose    bool
		processorNumber int
		vpnMode         bool
	)

	flag.StringVar(&configPath, "c", "./config.json", "config file path")
	flag.StringVar(&logPath, "l", "", "log file path")
	flag.BoolVar(&isLogVerbose, "v", false, "verbose mode")
	flag.IntVar(&processorNumber, "p", runtime.NumCPU(), "number of processor to use")
	flag.BoolVar(&vpnMode, "V", false, "VPN mode")
	flag.Parse()

	if isLogVerbose {
		log.SetLevel(log.DebugLevel)
	} else {
		log.SetLevel(log.InfoLevel)
	}

	log_init()

	log.Info("If you need any help, please visit the project repository: https://github.com/shadowsocks/overture")

	runtime.GOMAXPROCS(processorNumber)

	core.InitServer(configPath, vpnMode)
}
