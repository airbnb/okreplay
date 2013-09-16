package co.freeside.betamax.proxy.netty

import io.netty.handler.codec.http.*

class NettyResponseAdapterSpec extends NettyMessageAdapterSpec<FullHttpResponse, NettyResponseAdapter> {

	void setup() {
		nettyMessage = Mock(FullHttpResponse)
		adapter = new NettyResponseAdapter(nettyMessage)
	}

	void "can read response status"() {
		given:
		nettyMessage.status >> HttpResponseStatus.CREATED

		expect:
		adapter.status == HttpResponseStatus.CREATED.code()
	}

}

