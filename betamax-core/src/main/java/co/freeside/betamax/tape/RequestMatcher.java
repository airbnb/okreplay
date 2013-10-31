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

package co.freeside.betamax.tape;

import co.freeside.betamax.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.tape.*;

public class RequestMatcher {

    public RequestMatcher(Request request, Iterable<? extends MatchRule> rules) {
        this.request = request;
        this.rules = rules;
    }

    public boolean matches(final RecordedRequest recordedRequest) {
        for (MatchRule rule : rules) {
            if (!rule.isMatch(request, recordedRequest)) {
                return false;
            }
        }

        return true;
    }

    private final Iterable<? extends MatchRule> rules;
    private final Request request;
}
