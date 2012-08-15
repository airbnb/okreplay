package co.freeside.betamax.proxy.jetty

import co.freeside.betamax.util.SystemPropertyUtils

/**
 * A proxy selector implementation that uses any pre-existing proxy settings that were in effect before Betamax
 * hijacked the proxy settings.
 */
class OriginalProxySelector extends ProxySelector {

	@Override
	List<Proxy> select(URI uri) {
		def proxies = []
		if (uri.scheme.equalsIgnoreCase('http')) {
			addHttpProxy(proxies)
		} else if (uri.scheme.equalsIgnoreCase('https')) {
			addHttpsProxy(proxies)
		}
		proxies
	}

	@Override
	void connectFailed(URI uri, SocketAddress socketAddress, IOException e) { }

	private void addHttpProxy(List<Proxy> proxies) {
		if (SystemPropertyUtils.isOverridden('http.proxyHost')) {
			def host = SystemPropertyUtils.getOverriddenValue('http.proxyHost')
			def port = SystemPropertyUtils.getOverriddenValue('http.proxyPort').toInteger()
			def address = InetSocketAddress.createUnresolved(host, port)
			proxies << new Proxy(Proxy.Type.HTTP, address)
		}
	}

	private void addHttpsProxy(List<Proxy> proxies) {
		if (SystemPropertyUtils.isOverridden('https.proxyHost')) {
			def host = SystemPropertyUtils.getOverriddenValue('https.proxyHost')
			def port = SystemPropertyUtils.getOverriddenValue('https.proxyPort').toInteger()
			def address = InetSocketAddress.createUnresolved(host, port)
			proxies << new Proxy(Proxy.Type.HTTP, address)
		}
	}

}
