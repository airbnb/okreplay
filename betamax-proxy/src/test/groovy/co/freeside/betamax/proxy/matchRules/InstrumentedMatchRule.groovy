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

package co.freeside.betamax.proxy.matchRules

import java.util.concurrent.atomic.AtomicInteger
import co.freeside.betamax.MatchRule
import co.freeside.betamax.message.Request

class InstrumentedMatchRule implements MatchRule {

    def counter = new AtomicInteger(0)

    def requestValidations = []

    @Override
    boolean isMatch(Request a, Request b) {

        requestValidations.each { rv ->
            rv.call(a)
            rv.call(b)
        }

        def current = counter.incrementAndGet()
        println("Matching attempt: ${current}")
        println("A request class: ${a.getClass()}")
        println("B request class: ${b.getClass()}")

        if (a.uri == b.uri && a.method == b.method) {
            //Same method and URI, lets do a body comparison
            //Can only consume the body once, once it's gone it's gone.
            def aBody = a.bodyAsText.input.text
            def bBody = b.bodyAsText.input.text

            //Ideally in the real world, we'd parse the XML or the JSON and compare the ASTs instead
            // of just comparing the body strings, so that meaningless whitespace doesn't mean anything
            println("aBody:  |" + aBody + "|")
            println("bBody:  |" + bBody + "|")

            //Right now, lets just compare the bodies also
            return aBody == bBody
        } else {
            //URI and method don't match, so we're going to bail
            return false
        }
    }
}
