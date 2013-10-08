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

import java.net.*;
import org.apache.http.client.*;
import org.apache.http.conn.routing.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;

/**
 * A convenience extension of ProxySelectorRoutePlanner that will configure proxy selection in a way that will work with
 * Betamax.
 */
public class BetamaxRoutePlanner extends ProxySelectorRoutePlanner {

    public static void configure(AbstractHttpClient httpClient) {
        HttpRoutePlanner routePlanner = new BetamaxRoutePlanner(httpClient);
        httpClient.setRoutePlanner(routePlanner);
    }

    public BetamaxRoutePlanner(HttpClient httpClient) {
        this(httpClient.getConnectionManager().getSchemeRegistry());
    }

    public BetamaxRoutePlanner(SchemeRegistry schemeRegistry) {
        super(schemeRegistry, ProxySelector.getDefault());
    }
}
