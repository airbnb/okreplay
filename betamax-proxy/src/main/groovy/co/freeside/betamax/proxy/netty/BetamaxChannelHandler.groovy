/*
 * Copyright 2013 Rob Fletcher
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

package co.freeside.betamax.proxy.netty

import java.util.logging.Logger
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.Response
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import static java.util.logging.Level.SEVERE

@ChannelHandler.Sharable
public class BetamaxChannelHandler extends ChannelInboundHandlerAdapter {

	private HttpHandler handlerChain
	private boolean active = false

	private static final Logger log = Logger.getLogger(BetamaxChannelHandler.class.name)

	@Override
	void channelRead(ChannelHandlerContext context, Object message) {
		FullHttpRequest request = (FullHttpRequest) message
		def betamaxRequest = new NettyRequestAdapter(request)
		try {
			def betamaxResponse = handlerChain.handle(betamaxRequest)
			sendSuccess(context, betamaxResponse)
		} catch (HandlerException e) {
			log.log SEVERE, 'exception in proxy processing', e
			sendError(context, HttpResponseStatus.valueOf(e.httpStatus), e.message)
		} catch (Exception e) {
			log.log SEVERE, 'error recording HTTP exchange', e
			sendError(context, INTERNAL_SERVER_ERROR, e.message)
		}
	}

	@Override
	void channelActive(ChannelHandlerContext context) throws Exception {
		super.channelActive(context)
		active = true
	}

	@Override
	void channelInactive(ChannelHandlerContext context) throws Exception {
		super.channelInactive(context)
		active = false
	}

	boolean isActive() {
		active
	}

	HttpHandler leftShift(HttpHandler httpHandler) {
		handlerChain = httpHandler
		handlerChain
	}

	private void sendSuccess(ChannelHandlerContext context, Response betamaxResponse) {
		def status = HttpResponseStatus.valueOf(betamaxResponse.status)
		def content = betamaxResponse.hasBody() ? Unpooled.copiedBuffer(betamaxResponse.bodyAsBinary.bytes) : Unpooled.EMPTY_BUFFER
		FullHttpResponse response = betamaxResponse.hasBody() ? new DefaultFullHttpResponse(HTTP_1_1, status, content) : new DefaultFullHttpResponse(HTTP_1_1, status)
		for (Map.Entry<String, String> header : betamaxResponse.headers) {
			response.headers().set(header.key, header.value.split(/,\s*/).toList())
		}
		sendResponse(context, response)
	}

	private void sendError(ChannelHandlerContext context, HttpResponseStatus status, String message) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(message ?: "", CharsetUtil.UTF_8))
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8")
		sendResponse(context, response)
	}

	private void sendResponse(ChannelHandlerContext context, DefaultFullHttpResponse response) {
		context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
	}

}
