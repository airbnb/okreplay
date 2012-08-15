package co.freeside.betamax.proxy.jetty

import spock.lang.Specification
import static java.util.Collections.EMPTY_LIST
import co.freeside.betamax.util.SystemPropertyUtils

class OriginalProxySelectorSpec extends Specification {

	def proxySelector = new OriginalProxySelector()

	void 'returns an empty list if there were no pre-existing proxy settings'() {
		expect:
		proxySelector.select('http://freeside.co/betamax'.toURI()) == EMPTY_LIST
	}

	void 'returns proxy information if Betamax is overriding existing property settings'() {
		given:
		System.with {
			properties.'http.proxyHost' = 'myproxy'
			properties.'http.proxyPort' = '1337'
		}
		SystemPropertyUtils.with {
			override 'http.proxyHost', 'betamax'
			override 'http.proxyPort', '5555'
		}

		expect:
		def proxies = proxySelector.select('http://freeside.co/betamax'.toURI())
		proxies.size() == 1
		proxies.first().type() == Proxy.Type.HTTP
		proxies.first().address() == new InetSocketAddress('myproxy', 1337)

		cleanup:
		System.with {
			clearProperty 'http.proxyHost'
			clearProperty 'http.proxyPort'
		}
		SystemPropertyUtils.clearAll()
	}

}
