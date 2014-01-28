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

package co.freeside.betamax.proxy.netty;

import com.google.common.base.*;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.*;

public class PredicatedHttpFilters extends HttpFiltersAdapter {

    public static Predicate<HttpRequest> httpMethodPredicate(final HttpMethod method) {
        return new Predicate<HttpRequest>() {
            @Override
            public boolean apply(HttpRequest input) {
                return method.equals(input.getMethod());
            }
        };
    }

    private final HttpFilters delegate;
    private final Predicate<HttpRequest> predicate;

    public PredicatedHttpFilters(HttpFilters delegate, Predicate<HttpRequest> predicate, HttpRequest originalRequest) {
        super(originalRequest);
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public HttpResponse requestPre(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.requestPre(httpObject);
        } else {
            return null;
        }
    }

    @Override
    public HttpResponse requestPost(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.requestPost(httpObject);
        } else {
            return null;
        }
    }

    @Override
    public HttpObject responsePre(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.responsePre(httpObject);
        } else {
            return httpObject;
        }
    }

    @Override
    public HttpObject responsePost(HttpObject httpObject) {
        if (predicate.apply(originalRequest)) {
            return delegate.responsePost(httpObject);
        } else {
            return httpObject;
        }
    }
}
