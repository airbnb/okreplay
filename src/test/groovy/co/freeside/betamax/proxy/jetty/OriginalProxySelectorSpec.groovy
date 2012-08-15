package co.freeside.betamax.proxy.jetty

import co.freeside.betamax.util.SystemPropertyUtils
import spock.lang.Specification
import spock.lang.Unroll
import static java.util.Collections.EMPTY_LIST

@Unroll
class OriginalProxySelectorSpec extends Specification {

	private static final URI HTTP_URI = 'http://freeside.co/betamax'.toURI()
	private static final URI HTTPS_URI = 'https://github.com/robfletcher/betamax'.toURI()

	def proxySelector = new OriginalProxySelector()

	void cleanup() {
		System.with {
			clearProperty 'http.proxyHost'
			clearProperty 'http.proxyPort'
			clearProperty 'https.proxyHost'
			clearProperty 'https.proxyPort'
		}
		SystemPropertyUtils.clearAll()
	}

	void 'returns an empty list if there were no pre-existing proxy settings'() {
		expect:
		proxySelector.select(HTTP_URI) == EMPTY_LIST
	}

	void 'uses pre-existing #proxyScheme proxy if Betamax is overriding proxy settings'() {
		given:
		setupProxy proxyScheme, 'myproxy', 1337
		overrideProxy proxyScheme

		expect:
		def proxies = proxySelector.select(uri)
		proxies.size() == 1

		and:
		def proxy = proxies.first()
		proxy.type() == Proxy.Type.HTTP
		proxy.address() == new InetSocketAddress('myproxy', 1337)

		where:
		proxyScheme | uri
		'http'      | HTTP_URI
		'https'     | HTTPS_URI
	}

	void 'does not use #proxyScheme proxy for #uri'() {
		given:
		setupProxy proxyScheme, 'myproxy', 1337
		overrideProxy proxyScheme

		expect:
		def proxies = proxySelector.select(uri)
		proxies.size() == 1

		and:
		def proxy = proxies.first()
		proxy.type() == Proxy.Type.DIRECT

		where:
		proxyScheme | uri
		'http'      | HTTPS_URI
		'https'     | HTTP_URI
	}

	private void setupProxy(String scheme, String host, int port) {
		System.with {
			properties."${scheme}.proxyHost" = host
			properties."${scheme}.proxyPort" = port.toString()
		}
	}

	private void overrideProxy(String scheme) {
		SystemPropertyUtils.with {
			override "${scheme}.proxyHost", 'betamax'
			override "${scheme}.proxyPort", '5555'
		}
	}

}
