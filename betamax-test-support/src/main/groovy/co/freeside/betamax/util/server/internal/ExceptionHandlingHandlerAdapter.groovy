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

import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil

abstract class ExceptionHandlingHandlerAdapter extends ChannelInboundHandlerAdapter {

    @Override
    final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer("${cause.getClass().simpleName}: $cause.message", CharsetUtil.UTF_8)
        )
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain charset=UTF-8")
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }
}
