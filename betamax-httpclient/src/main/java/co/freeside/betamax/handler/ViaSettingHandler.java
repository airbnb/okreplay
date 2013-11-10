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

import static co.freeside.betamax.Headers.VIA_HEADER;
import static org.apache.http.HttpHeaders.VIA;

public class ViaSettingHandler extends ChainedHttpHandler {
    @Override
    public Response handle(Request request) {
        Response response = chain(request);
        response.addHeader(VIA, VIA_HEADER);
        return response;
    }
}
