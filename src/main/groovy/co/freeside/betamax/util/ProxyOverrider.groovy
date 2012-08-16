package co.freeside.betamax.util

import static java.net.Proxy.Type.HTTP

/**
 * Provides a mechanism to temporarily override current HTTP and HTTPS proxy settings and restore them later.
 */
class ProxyOverrider {

	private final Map<String, InetSocketAddress> originalProxies = [:]
	private final Collection<String> originalNonProxyHosts = new HashSet<String>()

	/**
	 * Activates a proxy override for the given URI scheme.
	 */
	void activate(String host, int port, Collection<String> nonProxyHosts) {
		for (scheme in ['http', 'https']) {
			def currentProxyHost = System.getProperty("${scheme}.proxyHost")
			def currentProxyPort = System.getProperty("${scheme}.proxyPort")
			if (currentProxyHost) {
				originalProxies[scheme] = InetSocketAddress.createUnresolved(currentProxyHost, currentProxyPort?.toInteger())
			}
			System.setProperty("${scheme}.proxyHost", host)
			System.setProperty("${scheme}.proxyPort", port.toString())
		}

		def currentNonProxyHosts = System.getProperty('http.nonProxyHosts')
		if (currentNonProxyHosts) {
			originalNonProxyHosts.addAll currentNonProxyHosts.tokenize('|')
		} else {
			originalNonProxyHosts.clear()
		}
		System.setProperty('http.nonProxyHosts', nonProxyHosts.join('|'))
	}

	/**
	 * Deactivates all proxy overrides restoring the pre-existing proxy settings if any.
	 */
	void deactivateAll() {
		for (scheme in ['http', 'https']) {
			def originalProxy = originalProxies.remove(scheme)
			if (originalProxy) {
				System.setProperty("${scheme}.proxyHost", originalProxy.hostName)
				System.setProperty("${scheme}.proxyPort", originalProxy.port.toString())
			} else {
				System.clearProperty("${scheme}.proxyHost")
				System.clearProperty("${scheme}.proxyPort")
			}
		}

		if (originalNonProxyHosts) {
			System.setProperty('http.nonProxyHosts', originalNonProxyHosts.join('|'))
		} else {
			System.clearProperty('http.nonProxyHosts')
		}
		originalNonProxyHosts.clear()
	}

	/**
	 * Used by the Betamax proxy so that it can use pre-existing proxy settings when forwarding requests that do not
	 * match anything on tape.
	 *
	 * @return a proxy selector that uses the overridden proxy settings if any.
	 */
	ProxySelector getOriginalProxySelector() {
		new ProxySelector() {
			@Override
			List<Proxy> select(URI uri) {
				def address = originalProxies[uri.scheme]
				if (address && !(uri.host in originalNonProxyHosts)) {
					[new Proxy(HTTP, address)]
				} else {
					[Proxy.NO_PROXY]
				}
			}

			@Override
			void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
		}
	}

}
