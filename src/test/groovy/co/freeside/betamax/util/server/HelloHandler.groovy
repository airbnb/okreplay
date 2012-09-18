package co.freeside.betamax.util.server

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static java.net.HttpURLConnection.HTTP_OK

class HelloHandler extends AbstractHandler {

	public static final String HELLO_WORLD = 'Hello World!'

	@Override
    void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.status = HTTP_OK
        response.contentType = 'text/plain'
        response.outputStream.withWriter { writer ->
            writer << HELLO_WORLD
        }
    }
}
