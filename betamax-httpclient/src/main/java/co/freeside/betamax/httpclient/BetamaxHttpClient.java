/*
 * Copyright 2012 the original author or authors.
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

package co.freeside.betamax.httpclient;

import co.freeside.betamax.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.conn.*;
import org.apache.http.conn.routing.*;
import org.apache.http.impl.client.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;

public class BetamaxHttpClient extends DefaultHttpClient {

    private final Configuration configuration;
    private final Recorder recorder;

    public BetamaxHttpClient(Configuration configuration, Recorder recorder) {
        this.configuration = configuration;
        this.recorder = recorder;
    }

    @Override
    protected RequestDirector createClientRequestDirector(
            HttpRequestExecutor requestExec,
            ClientConnectionManager conman,
            ConnectionReuseStrategy reustrat,
            ConnectionKeepAliveStrategy kastrat,
            HttpRoutePlanner rouplan,
            HttpProcessor httpProcessor,
            HttpRequestRetryHandler retryHandler,
            RedirectStrategy redirectStrategy,
            AuthenticationStrategy targetAuthStrategy,
            AuthenticationStrategy proxyAuthStrategy,
            UserTokenHandler userTokenHandler,
            HttpParams params) {
        RequestDirector director = super.createClientRequestDirector(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectStrategy, targetAuthStrategy, proxyAuthStrategy, userTokenHandler, params);
        return new BetamaxRequestDirector(director, configuration, recorder, getCredentialsProvider());
    }

}

