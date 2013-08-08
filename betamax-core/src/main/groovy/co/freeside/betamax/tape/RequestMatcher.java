/*
 * Copyright 2011 Rob Fletcher
 *
 * Converted from Groovy to Java by Sean Freitag
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.tape;

import java.util.Comparator;

import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.tape.RecordedRequest;

public class RequestMatcher {
    
    @SuppressWarnings("unchecked")
    public RequestMatcher(Request request) {
        this(request, new Comparator<Request>() {
            @Override
            public int compare(Request o1, Request o2) {
                int result = o1.getMethod().compareTo(o2.getMethod());
                if (result != 0)
                    return result;
                return o1.getUri().compareTo(o2.getUri());
            }
        });
    }

    public RequestMatcher(Request request, Comparator<Request>... rules) {
        this.request = request;
        this.rules = rules;
    }

    public boolean matches(final RecordedRequest recordedRequest) {
        for (Comparator<Request> comparator : rules) {
            if (comparator.compare(request, recordedRequest) != 0)
                return false;
        }

        return true;
    }

    private final Comparator<Request>[] rules;
    private final Request request;
}
