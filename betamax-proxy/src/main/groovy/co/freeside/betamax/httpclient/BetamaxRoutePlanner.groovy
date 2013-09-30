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

package co.freeside.betamax.httpclient

import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner

/**
 * A convenience extension of ProxySelectorRoutePlanner that will configure proxy selection in a way that will work with
 * Betamax.
 */
class BetamaxRoutePlanner extends ProxySelectorRoutePlanner {

    static void configure(AbstractHttpClient httpClient) {
        def routePlanner = new BetamaxRoutePlanner(httpClient)
		httpClient.routePlanner = routePlanner
    }

    BetamaxRoutePlanner(HttpClient httpClient) {
        this(httpClient.connectionManager.schemeRegistry)
    }

    BetamaxRoutePlanner(SchemeRegistry schemeRegistry) {
        super(schemeRegistry, ProxySelector.default)
    }
}
