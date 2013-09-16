package co.freeside.betamax.proxy.netty

import io.netty.handler.codec.http.FullHttpRequest
import static io.netty.handler.codec.http.HttpMethod.GET

class NettyRequestAdapterSpec extends NettyMessageAdapterSpec<FullHttpRequest, NettyRequestAdapter> {

	void setup() {
		nettyMessage = Mock(FullHttpRequest)
		adapter = new NettyRequestAdapter(nettyMessage)
	}

	void 'can read basic fields'() {
		given:
		nettyMessage.method >> GET
		nettyMessage.uri >> 'http://freeside.co/betamax'

		expect:
		adapter.method == 'GET'
		adapter.uri == 'http://freeside.co/betamax'.toURI()
	}

	void 'uri includes query string'() {
		given:
		nettyMessage.uri >> 'http://freeside.co/betamax?q=1'

		expect:
		adapter.uri == new URI('http://freeside.co/betamax?q=1')
	}
}
