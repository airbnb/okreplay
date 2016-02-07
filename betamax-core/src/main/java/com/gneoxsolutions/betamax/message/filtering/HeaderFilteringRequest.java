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

package com.gneoxsolutions.betamax.message.filtering;

import com.gneoxsolutions.betamax.message.Message;
import com.gneoxsolutions.betamax.message.Request;

import java.net.URI;

public class HeaderFilteringRequest extends HeaderFilteringMessage implements Request {
    public HeaderFilteringRequest(Request request) {
        this.request = request;
    }

    protected Message getDelegate() {
        return request;
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public URI getUri() {
        return request.getUri();
    }

    private final Request request;
}
