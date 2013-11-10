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

package co.freeside.betamax.handler;

import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;

public abstract class ChainedHttpHandler implements HttpHandler {

    protected final Response chain(Request request) {
        if (next == null) {
            throw new IllegalStateException("attempted to chain from the last handler in the chain");
        }

        return next.handle(request);
    }

    public final void setNext(HttpHandler next) {
        this.next = next;
    }

    public final HttpHandler add(HttpHandler next) {
        setNext(next);
        return next;
    }

    public final ChainedHttpHandler add(ChainedHttpHandler next) {
        setNext(next);
        return next;
    }

    private HttpHandler next;
}
