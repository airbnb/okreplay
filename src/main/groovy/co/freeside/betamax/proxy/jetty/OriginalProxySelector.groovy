package co.freeside.betamax.proxy.jetty

import static java.util.Collections.EMPTY_LIST
import co.freeside.betamax.util.SystemPropertyUtils

/**
 * A proxy selector implementation that uses any pre-existing proxy settings that were in effect before Betamax
 * hijacked the proxy settings.
 */
class OriginalProxySelector extends ProxySelector {

	@Override
	List<Proxy> select(URI uri) {
		if (SystemPropertyUtils.isOverridden('http.proxyHost')) {
			def host = SystemPropertyUtils.getOverriddenValue('http.proxyHost')
			def port = SystemPropertyUtils.getOverriddenValue('http.proxyPort').toInteger()
			def address = InetSocketAddress.createUnresolved(host, port)
			[new Proxy(Proxy.Type.HTTP, address)]
		} else {
			EMPTY_LIST
		}
	}

	@Override
	void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
	}
}
