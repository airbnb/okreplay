package betamax.server

import betamax.Recorder
import groovy.util.logging.Log4j
import org.apache.http.impl.nio.DefaultServerIOEventDispatch
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor
import org.apache.http.nio.NHttpConnection
import org.apache.http.nio.reactor.IOReactor
import org.apache.http.params.SyncBasicHttpParams
import org.apache.http.*
import org.apache.http.impl.*
import org.apache.http.nio.protocol.*
import static org.apache.http.params.CoreConnectionPNames.*
import static org.apache.http.params.CoreProtocolPNames.ORIGIN_SERVER
import org.apache.http.protocol.*
import org.apache.http.nio.reactor.IOReactorStatus

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 server based on the non-blocking 
 * I/O model.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs.
 * It is NOT intended to demonstrate the most efficient way of building an HTTP server. 
 */
@Log4j
class HttpProxyServer implements EventListener {

	private IOReactor reactor
	final int port

    private String originalProxyHost
    private String originalProxyPort

    HttpProxyServer() {
		port = 5555
	}

	void start(Recorder recorder) {
		def params = new SyncBasicHttpParams()
		params.setIntParameter(SO_TIMEOUT, 5000)
		params.setIntParameter(SOCKET_BUFFER_SIZE, 8 * 1024)
		params.setBooleanParameter(STALE_CONNECTION_CHECK, false)
		params.setBooleanParameter(TCP_NODELAY, true)
		params.setParameter(ORIGIN_SERVER, "HttpComponents/1.1")

		def httpproc = new ImmutableHttpProcessor([
				new ResponseDate(),
				new ResponseServer(),
				new ResponseContent(),
				new ResponseConnControl()
		] as HttpResponseInterceptor[])

		def handler = new BufferingHttpServiceHandler(
				httpproc,
				new DefaultHttpResponseFactory(),
				new DefaultConnectionReuseStrategy(),
				params)

		def reqistry = new HttpRequestHandlerRegistry()
		reqistry.register "*", new HttpProxyHandler(recorder)

		handler.handlerResolver = reqistry
		handler.eventListener = this

		def ioEventDispatch = new DefaultServerIOEventDispatch(handler, params)
		reactor = new DefaultListeningIOReactor(2, params)
		reactor.listen(new InetSocketAddress(port))

        overrideProxySettings()

		Thread.start {
			log.debug "starting proxy server..."
			reactor.execute(ioEventDispatch)
		}

		while (reactor.status != IOReactorStatus.ACTIVE) {
			log.debug "waiting..."
			sleep 100
		}
	}

	void stop() {
		log.debug "stopping proxy server..."
        restoreOriginalProxySettings()
		reactor.shutdown()
	}

    private void overrideProxySettings() {
        originalProxyHost = System.properties."http.proxyHost"
        originalProxyPort = System.properties."http.proxyPort"
        System.properties."http.proxyHost" = "localhost"
        System.properties."http.proxyPort" = port.toString()
    }

    private void restoreOriginalProxySettings() {
        if (originalProxyHost) {
            System.properties."http.proxyHost" = originalProxyHost
        } else {
            System.clearProperty("http.proxyHost")
        }
        if (originalProxyPort) {
            System.properties."http.proxyPort" = originalProxyPort
        } else {
            System.clearProperty("http.proxyPort")
        }
    }

	void connectionOpen(final NHttpConnection conn) {
		log.info "Connection open: $conn"
	}

	void connectionTimeout(final NHttpConnection conn) {
		log.info "Connection timed out: $conn"
	}

	void connectionClosed(final NHttpConnection conn) {
		log.info "Connection closed: $conn"
	}

	void fatalIOException(final IOException ex, final NHttpConnection conn) {
		log.error "I/O error: $ex.message"
	}

	void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
		log.error "HTTP error: $ex.message"
	}

}


