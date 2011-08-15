package betamax.server

import org.apache.http.nio.NHttpConnection
import org.apache.http.HttpException

class EventLogger implements org.apache.http.nio.protocol.EventListener {

    void connectionOpen(final NHttpConnection conn) {
        println "Connection open: $conn"
    }

    void connectionTimeout(final NHttpConnection conn) {
        println "Connection timed out: $conn"
    }

    void connectionClosed(final NHttpConnection conn) {
        println "Connection closed: $conn"
    }

    void fatalIOException(final IOException ex, final NHttpConnection conn) {
        System.err.println("I/O error: $ex.message")
    }

    void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
        System.err.println("HTTP error: $ex.message")
    }

}
