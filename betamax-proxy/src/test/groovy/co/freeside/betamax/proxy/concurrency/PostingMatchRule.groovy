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

package co.freeside.betamax.proxy.concurrency

import co.freeside.betamax.MatchRule
import co.freeside.betamax.message.Request

import java.util.concurrent.atomic.AtomicInteger

class PostingMatchRule implements MatchRule {

    @Override
    boolean isMatch(Request a, Request b) {
        if (a.uri == b.uri && a.method == b.method) {
            //Same method and URI, lets do a body comparison
            //Can only consume the body once, once it's gone it's gone.
            def aBody = a.bodyAsText.input.text
            def bBody = b.bodyAsText.input.text

            //Right now, lets just compare the bodies also
            return aBody == bBody
        } else {
            //URI and method don't match, so we're going to bail
            return false
        }
    }
}
