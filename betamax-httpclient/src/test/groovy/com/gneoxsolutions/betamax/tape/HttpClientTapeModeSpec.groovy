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

package com.gneoxsolutions.betamax.tape

import com.gneoxsolutions.betamax.handler.DefaultHandlerChain
import com.gneoxsolutions.betamax.util.message.BasicRequest
import com.gneoxsolutions.betamax.Configuration
import com.gneoxsolutions.betamax.tck.TapeModeSpec

class HttpClientTapeModeSpec extends TapeModeSpec {

    def handler = new DefaultHandlerChain(recorder)
    def request = new BasicRequest("GET", endpoint.url)

    @Override
    protected Configuration getConfiguration() {
        Configuration.builder().tapeRoot(tapeRoot).build()
    }

    protected void makeRequest() {
        handler.handle(request)
    }
}
