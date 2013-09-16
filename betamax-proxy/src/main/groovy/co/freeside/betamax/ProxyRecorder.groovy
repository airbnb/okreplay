package co.freeside.betamax

import co.freeside.betamax.proxy.ProxyServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import co.freeside.betamax.util.PropertiesCategory
import groovy.transform.InheritConstructors
import org.apache.http.conn.ssl.SSLSocketFactory

@InheritConstructors
class ProxyRecorder extends Recorder {

	public static final int DEFAULT_PROXY_PORT = 5555
	public static final int DEFAULT_PROXY_TIMEOUT = 5000
	public static final SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY = DummySSLSocketFactory.instance

	/**
	 * The port the Betamax proxy will listen on.
	 */
	int proxyPort

	/**
	 * The time (in milliseconds) the proxy will wait before aborting a request.
	 */
	int proxyTimeout

	/**
	 * If set to true add support for proxying SSL (disable certificate checking).
	 */
	boolean sslSupport

	/**
	 * The factory that will be used to create SSL sockets for secure connections to the target.
	 */
	SSLSocketFactory sslSocketFactory

	private ProxyServer interceptor

	/**
	 * @return the hostname or address where the proxy will run.
	 */
	String getProxyHost() {
		interceptor.host
	}

	/**
	 * @return a `java.net.Proxy` instance configured to point to the Betamax proxy.
	 */
	Proxy getProxy() {
		new Proxy(Proxy.Type.HTTP, new InetSocketAddress(interceptor.host, interceptor.port))
	}

	@Override
	void start(String tapeName, Map arguments) {
		if (!interceptor) {
			interceptor = new ProxyServer(this)
		}
		if (!interceptor.running) {
			interceptor.start()
		}
		super.start(tapeName, arguments)
	}

	@Override
	void stop() {
		interceptor.stop()
		super.stop()
	}

	@Override
	protected void configureFrom(Properties properties) {
		super.configureFrom(properties)

		use(PropertiesCategory) {
			proxyPort = properties.getInteger('betamax.proxyPort', DEFAULT_PROXY_PORT)
			proxyTimeout = properties.getInteger('betamax.proxyTimeout', DEFAULT_PROXY_TIMEOUT)
			sslSupport = properties.getBoolean('betamax.sslSupport')
		}
	}

	@Override
	protected void configureFrom(ConfigObject config) {
		super.configureFrom(config)

		proxyPort = config.betamax.proxyPort ?: DEFAULT_PROXY_PORT
		proxyTimeout = config.betamax.proxyTimeout ?: DEFAULT_PROXY_TIMEOUT
		sslSupport = config.betamax.sslSupport
		sslSocketFactory = config.betamax.sslSocketFactory ?: DEFAULT_SSL_SOCKET_FACTORY
	}

	@Override
	protected void configureWithDefaults() {
		super.configureWithDefaults()

		proxyPort = DEFAULT_PROXY_PORT
		proxyTimeout = DEFAULT_PROXY_TIMEOUT
		sslSupport = false
		sslSocketFactory = DEFAULT_SSL_SOCKET_FACTORY
	}
}
