package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.util.PropertiesCategory
import groovy.transform.InheritConstructors

@InheritConstructors
class BetamaxProxyRecorder extends Recorder {

	public static final int DEFAULT_PROXY_PORT = 5555
	public static final int DEFAULT_PROXY_TIMEOUT = 5000

	/**
	 * The port the Betamax proxy will listen on.
	 */
	int proxyPort

	/**
	 * The time the proxy will wait before aborting a request in milliseconds.
	 */
	int proxyTimeout

	/**
	 * If set to true the proxy is started before and stopped after each annotated test method.
	 */
	@Deprecated boolean useProxy

	/**
	 * If set to true add support for proxying SSL (disable certificate checking)
	 */
	boolean sslSupport

	private final ProxyServer interceptor = new ProxyServer(this)

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
	protected void configureFromProperties(Properties properties) {
		super.configureFromProperties(properties)

		use(PropertiesCategory) {
			proxyPort = properties.getInteger('betamax.proxyPort', DEFAULT_PROXY_PORT)
			proxyTimeout = properties.getInteger('betamax.proxyTimeout', DEFAULT_PROXY_TIMEOUT)
			sslSupport = properties.getBoolean('betamax.sslSupport')
			useProxy = properties.getBoolean('betamax.useProxy')
		}
	}

	@Override
	protected void configureFromConfig(ConfigObject config) {
		super.configureFromConfig(config)

		proxyPort = config.betamax.proxyPort ?: DEFAULT_PROXY_PORT
		proxyTimeout = config.betamax.proxyTimeout ?: DEFAULT_PROXY_TIMEOUT
		sslSupport = config.betamax.sslSupport
		useProxy = config.betamax.useProxy
	}

	@Override
	protected void configureWithDefaults() {
		super.configureWithDefaults()

		proxyPort = DEFAULT_PROXY_PORT
		proxyTimeout = DEFAULT_PROXY_TIMEOUT
		sslSupport = false
		useProxy = true
	}
}
