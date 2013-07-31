package co.freeside.betamax.proxy.netty

import spock.lang.Specification

class NettyServerSpec extends Specification {

	void "can serve HTTP responses with Netty"() {
		given:
		def channelInitializer = new HttpChannelInitializer(0, new EchoServerHandler())
		def server = new NettyBetamaxServer(port, channelInitializer)
		server.run()

		when:
		HttpURLConnection connection = new URL("http://localhost:$port/").openConnection()
		connection.requestMethod = "POST"
		connection.doInput = true
		connection.doOutput = true
		connection.outputStream.withWriter("UTF-8") { writer ->
			writer << message
		}
		connection.connect()

		then:
		connection.inputStream.getText("UTF-8") == message

		cleanup:
		server.shutdown()

		where:
		port = 5000
		message = "O HAI"
	}

}