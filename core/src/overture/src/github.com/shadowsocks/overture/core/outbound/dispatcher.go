package outbound

import (
	"net"
	"regexp"
	"runtime"

	log "github.com/Sirupsen/logrus"
	"github.com/miekg/dns"
	"github.com/shadowsocks/overture/core/cache"
	"github.com/shadowsocks/overture/core/common"
	"github.com/shadowsocks/overture/core/hosts"
)

type Dispatcher struct {
	QuestionMessage *dns.Msg

	PrimaryDNS     []*common.DNSUpstream
	AlternativeDNS []*common.DNSUpstream
	OnlyPrimaryDNS bool

	PrimaryClientBundle     *ClientBundle
	AlternativeClientBundle *ClientBundle
	ActiveClientBundle      *ClientBundle

	IPNetworkList      []*net.IPNet
	AclList            []string
	RedirectIPv6Record bool

	InboundIP string

	Hosts *hosts.Hosts
	Cache *cache.Cache
}

func (d *Dispatcher) Exchange() {

	d.PrimaryClientBundle = NewClientBundle(d.QuestionMessage, d.PrimaryDNS, d.InboundIP, d.Hosts, d.Cache)
	d.AlternativeClientBundle = NewClientBundle(d.QuestionMessage, d.AlternativeDNS, d.InboundIP, d.Hosts, d.Cache)

	for _, cb := range [2]*ClientBundle{d.PrimaryClientBundle, d.AlternativeClientBundle} {
		if ok := cb.ExchangeFromLocal(); ok {
			d.ActiveClientBundle = cb
			return
		}
	}

	if d.OnlyPrimaryDNS {
		d.ActiveClientBundle = d.PrimaryClientBundle
		d.ActiveClientBundle.ExchangeFromRemote(true, true)
		return
	}

	if ok := d.ExchangeForIPv6() || d.ExchangeForDomain(); ok {
		d.AlternativeClientBundle.ExchangeFromRemote(true, true)
		return
	}

	d.ChooseActiveClientBundle()
	if d.ActiveClientBundle == d.AlternativeClientBundle {
		d.ActiveClientBundle.ExchangeFromRemote(false, true)
	}
	d.ActiveClientBundle.CacheResult()
}

func (d *Dispatcher) ExchangeForIPv6() bool {

	if (d.PrimaryClientBundle.QuestionMessage.Question[0].Qtype == dns.TypeAAAA) && d.RedirectIPv6Record {
		d.ActiveClientBundle = d.AlternativeClientBundle
		log.Debug("Finally use alternative DNS")
		return true
	}

	return false
}

func (d *Dispatcher) ExchangeForDomain() bool {

	qn := d.PrimaryClientBundle.QuestionMessage.Question[0].Name[:len(d.PrimaryClientBundle.QuestionMessage.Question[0].Name)-1]

	for _, re := range d.AclList {
		matched, err := regexp.MatchString(re, qn)
		if err == nil && matched {
			log.Debug("Matched: Custom domain " + qn + " with " + re)
			d.ActiveClientBundle = d.AlternativeClientBundle
			log.Debug("Finally use alternative DNS")
			return true
		}
	}

	runtime.GC()

	log.Debug("Domain match fail, try to use primary DNS")

	return false
}

func (d *Dispatcher) ChooseActiveClientBundle() {

	d.ActiveClientBundle = d.PrimaryClientBundle
	d.PrimaryClientBundle.ExchangeFromRemote(false, true)

	if d.PrimaryClientBundle.ResponseMessage == nil || !common.HasAnswer(d.PrimaryClientBundle.ResponseMessage) {
		log.Debug("Primary DNS answer is empty, finally use alternative DNS")
		d.ActiveClientBundle = d.AlternativeClientBundle
		return
	}

	for _, a := range d.PrimaryClientBundle.ResponseMessage.Answer {
		if a.Header().Rrtype == dns.TypeA {
			log.Debug("Try to match response ip address with IP network")
			if common.IsIPMatchList(net.ParseIP(a.(*dns.A).A.String()), d.IPNetworkList, true) {
				break
			}
		} else if a.Header().Rrtype == dns.TypeAAAA {
			log.Debug("Try to match response ip address with IP network")
			if common.IsIPMatchList(net.ParseIP(a.(*dns.AAAA).AAAA.String()), d.IPNetworkList, true) {
				break
			}
		} else {
			continue
		}

		log.Debug("IP network match fail, finally use alternative DNS")
		d.ActiveClientBundle = d.AlternativeClientBundle
		return
	}

	log.Debug("Finally use primary DNS")
	d.ActiveClientBundle = d.PrimaryClientBundle
}
