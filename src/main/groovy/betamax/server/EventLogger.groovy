package betamax.server

import groovy.util.logging.Log4j
import org.apache.http.HttpException
import org.apache.http.nio.NHttpConnection

@Log4j
class EventLogger implements org.apache.http.nio.protocol.EventListener {

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
