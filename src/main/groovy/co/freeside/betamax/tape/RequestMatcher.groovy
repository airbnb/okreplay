/*
 * Copyright 2011 Rob Fletcher
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

package co.freeside.betamax.tape

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.tape.RecordedRequest

import static co.freeside.betamax.MatchRule.method
import static co.freeside.betamax.MatchRule.uri

class RequestMatcher {

	private final Comparator<Request>[] rules
	private final Request request

	RequestMatcher(Request request) {
		this(request, [method, uri] as Comparator<Request>[]) // not sure why varargs doesn't want to work here
	}

	RequestMatcher(Request request, Comparator<Request>... rules) {
		this.request = request
		this.rules = rules
	}

	boolean matches(RecordedRequest recordedRequest) {
		rules.every { it.compare(request, recordedRequest) == 0 }
	}

}

