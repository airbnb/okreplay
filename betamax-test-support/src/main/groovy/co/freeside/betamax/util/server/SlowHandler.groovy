/*
 * Copyright 2011 the original author or authors.
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

package co.freeside.betamax.util.server

import co.freeside.betamax.util.server.internal.ExceptionHandlingHandlerAdapter
import groovy.util.logging.Log
import io.netty.channel.*
import io.netty.handler.codec.http.HttpRequest

/**
 * A very dumb handler that will simply sit on any requests until it is told to shut down (i.e. the server is shutting
 * down). This is used for testing timeout conditions on clients.
 */
@Log
@ChannelHandler.Sharable
class SlowHandler extends ExceptionHandlingHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.fine "received ${((HttpRequest) msg).method} request..."
    }

}
