/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.util.server.internal

import io.netty.channel.*
import io.netty.handler.codec.http.*
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8
import static io.netty.buffer.Unpooled.copiedBuffer
import static io.netty.channel.ChannelFutureListener.CLOSE
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import static io.netty.util.CharsetUtil.UTF_8

abstract class ExceptionHandlingHandlerAdapter extends ChannelInboundHandlerAdapter {

    @Override
    final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                INTERNAL_SERVER_ERROR,
                copiedBuffer("${cause.getClass().simpleName}: $cause.message", UTF_8)
        )
        response.headers().set(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString())
        ctx.writeAndFlush(response).addListener(CLOSE)
    }
}
