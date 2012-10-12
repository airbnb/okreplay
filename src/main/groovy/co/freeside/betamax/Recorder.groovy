/*
 * Copyright 2011 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax

import java.util.logging.Logger
import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.tape.*
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.*
import org.junit.rules.MethodRule
import org.junit.runners.model.*
import org.yaml.snakeyaml.introspector.PropertyUtils
import static TapeMode.READ_WRITE
import static co.freeside.betamax.MatchRule.*
import static java.util.Collections.EMPTY_MAP
/**
 * This is the main interface to the Betamax proxy. It allows control of Betamax configuration and inserting and
 * ejecting `Tape` instances. The class can also be used as a _JUnit @Rule_ allowing tests annotated with `@Betamax` to
 * run with the Betamax HTTP proxy in the background.
 */
class Recorder implements MethodRule {

	public static final String DEFAULT_TAPE_ROOT = 'src/test/resources/betamax/tapes'
	public static final int DEFAULT_PROXY_PORT = 5555
	public static final int DEFAULT_PROXY_TIMEOUT = 5000

	private static final log = Logger.getLogger(Recorder.name)

	Recorder() {
		def configFile = getClass().classLoader.getResource('BetamaxConfig.groovy')
		if (configFile) {
			def config = new ConfigSlurper().parse(configFile)
			configureFromConfig(config)
		} else {
			def propertiesFile = getClass().classLoader.getResource('betamax.properties')
			if (propertiesFile) {
				def properties = new Properties()
				propertiesFile.withReader { reader ->
					properties.load(reader)
				}
				configureFromProperties(properties)
			}
		}
	}

	Recorder(Properties properties) {
		configureFromProperties(properties)
	}

	/**
	 * The port the Betamax proxy will listen on.
	 */
	int proxyPort = DEFAULT_PROXY_PORT

	/**
	 * The base directory where tape files are stored.
	 */
	File tapeRoot = new File(DEFAULT_TAPE_ROOT)

	/**
	 * The default mode for an inserted tape.
	 */
	TapeMode defaultMode = READ_WRITE

	/**
	 * The time the proxy will wait before aborting a request in milliseconds.
	 */
	int proxyTimeout = DEFAULT_PROXY_TIMEOUT

	/**
	 * Hosts that are ignored by the proxy. Any connections made will be allowed to proceed normally and not be
	 * intercepted.
	 */
	Collection<String> ignoreHosts = []

	/**
	 * If set to true all connections to localhost addresses are ignored.
	 * This is equivalent to setting `ignoreHosts` to `['localhost', '127.0.0.1', InetAddress.localHost.hostName,
	 * InetAddress.localHost.hostAddress]`.
	 */
	boolean ignoreLocalhost = false

	/**
	 * If set to true add support for proxying SSL (disable certificate checking)
	 */
	boolean sslSupport = false


	String getProxyHost() {
		proxy.url.toURI().host
	}

	int getHttpsProxyPort() {
		//proxyPort + 1
		proxyPort
	}

	private StorableTape tape
	private final HttpInterceptor proxy = new ProxyServer(this)

	/**
	 * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
	 * @param name the name of the _tape_.
	 * @param arguments customize the behaviour of the tape.
	 */
	void insertTape(String name, Map arguments = [:]) {
		tape = tapeLoader.loadTape(name)
		tape.mode = arguments.mode ?: defaultMode
		tape.matchRules = arguments.match ?: [method, uri]
		tape
	}

	/**
	 * Gets the current active _tape_.
	 * @return the active _tape_.
	 */
	Tape getTape() {
		tape
	}

	/**
	 * 'Ejects' the current _tape_, writing its content to file. If the proxy is active after calling this method it
	 * will no longer record or play back any HTTP traffic until another tape is inserted.
	 */
	void ejectTape() {
		if (tape) {
			tapeLoader.writeTape(tape)
			tape = null
		}
	}

	/**
	 * Runs the supplied closure after starting the Betamax proxy and inserting a _tape_. After the closure completes
	 * the _tape_ is ejected and the proxy stopped.
	 * @param name the name of the _tape_.
	 * @param closure the closure to execute.
	 * @return the return value of the closure.
	 */
	def withTape(String name, Closure closure) {
		withTape(name, EMPTY_MAP, closure)
	}

	/**
	 * Runs the supplied closure after starting the Betamax proxy and inserting a _tape_. After the closure completes
	 * the _tape_ is ejected and the proxy stopped.
	 * @param tapeName the name of the _tape_.
	 * @param arguments arguments that affect the operation of the proxy.
	 * @param closure the closure to execute.
	 * @return the return value of the closure.
	 */
	def withTape(String tapeName, Map arguments, Closure closure) {
		try {
			startProxy(tapeName, arguments)
			closure()
		} finally {
			stopProxy()
		}
	}

	Proxy getProxy() {
		new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.localHost.hostAddress, proxyPort))
	}

	@Override
	Statement apply(Statement statement, FrameworkMethod method, Object target) {
		def annotation = method.getAnnotation(Betamax)
		if (annotation) {
			log.fine "found @Betamax annotation on '$method.name'"
			new Statement() {
				void evaluate() {
					withTape(annotation.tape(), [mode: annotation.mode(), match: annotation.match()]) {
						statement.evaluate()
					}
				}
			}
		} else {
			log.fine "no @Betamax annotation on '$method.name'"
			statement
		}
	}

	private void startProxy(String tapeName, Map arguments) {
		if (!proxy.running) {
			proxy.start()
		}
		insertTape(tapeName, arguments)
	}

	private void stopProxy() {
		proxy.stop()
		ejectTape()
	}

	private TapeLoader getTapeLoader() {
		new YamlTapeLoader(tapeRoot)
	}

	private void configureFromProperties(Properties properties) {
		use(PropertiesCategory) {
			tapeRoot = new File(properties.getProperty('betamax.tapeRoot', DEFAULT_TAPE_ROOT))
			proxyPort = properties.getInteger('betamax.proxyPort', DEFAULT_PROXY_PORT)
			proxyTimeout = properties.getInteger('betamax.proxyTimeout', DEFAULT_PROXY_TIMEOUT)
			defaultMode = properties.getEnum('betamax.defaultMode', READ_WRITE)
			ignoreHosts = properties.getProperty('betamax.ignoreHosts')?.tokenize(',') ?: []
			ignoreLocalhost = properties.getBoolean('betamax.ignoreLocalhost')
			sslSupport = properties.getBoolean('betamax.sslSupport')
		}
	}

	private void configureFromConfig(ConfigObject config) {
		tapeRoot = config.betamax.tapeRoot ?: DEFAULT_TAPE_ROOT
		proxyPort = config.betamax.proxyPort ?: DEFAULT_PROXY_PORT
		proxyTimeout = config.betamax.proxyTimeout ?: DEFAULT_PROXY_TIMEOUT
		defaultMode = config.betamax.defaultMode ?: READ_WRITE
		ignoreHosts = config.betamax.ignoreHosts ?: []
		ignoreLocalhost = config.betamax.ignoreLocalhost
		sslSupport = config.betamax.sslSupport
	}

}
