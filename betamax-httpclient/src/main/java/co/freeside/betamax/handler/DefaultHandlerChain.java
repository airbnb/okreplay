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

import co.freeside.betamax.*;
import co.freeside.betamax.message.*;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;

/**
 * The default handler chain used by all Betamax implementations.
 */
public class DefaultHandlerChain extends ChainedHttpHandler {
    public DefaultHandlerChain(Recorder recorder, HttpClient httpClient) {
        this.add(new ViaSettingHandler())
            .add(new TapeReader(recorder))
            .add(new TapeWriter(recorder))
            .add(new HeaderFilter())
            .add(new TargetConnector(httpClient));
    }

    public DefaultHandlerChain(Recorder recorder) {
        this(recorder, newHttpClient());
    }

    private static HttpClient newHttpClient() {
        return HttpClients.createMinimal();
    }

    @Override
    public Response handle(Request request) {
        return chain(request);
    }
}
