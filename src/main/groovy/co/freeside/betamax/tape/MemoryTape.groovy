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

import java.util.concurrent.atomic.AtomicInteger
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.*
import co.freeside.betamax.message.tape.*
import org.yaml.snakeyaml.reader.StreamReader
import static TapeMode.READ_WRITE
import static co.freeside.betamax.MatchRule.*
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static org.apache.http.HttpHeaders.VIA

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
class MemoryTape implements Tape {

	String name
	List<RecordedInteraction> interactions = []
	private TapeMode mode = READ_WRITE
	private AtomicInteger orderedIndex = new AtomicInteger()
	private Comparator<Request>[] matchRules = [method, uri]

	void setMode(TapeMode mode) {
		this.mode = mode
	}

	void setMatchRules(Comparator<Request>[] matchRules) {
		this.matchRules = matchRules
	}

	boolean isReadable() {
		mode.readable
	}

	boolean isWritable() {
		mode.writable
	}

	boolean isSequential() {
		mode.sequential
	}

	int size() {
		interactions.size()
	}

	boolean seek(Request request) {
		if (sequential) {
			// TODO: it's a complete waste of time using an AtomicInteger when this method is called before play in a non-transactional way
			def index = orderedIndex.get()
			def nextRequest = interactions[index]?.request
			def requestMatcher = new RequestMatcher(request, matchRules)
			nextRequest && requestMatcher.matches(nextRequest)
		} else {
			findMatch(request) >= 0
		}
	}

	boolean isAtEnd() {
		sequential && !writable && orderedIndex.get() >= interactions.size()
	}

	Response play(Request request) {
		if (!mode.readable) {
			throw new IllegalStateException('the tape is not readable')
		}

		if (mode.sequential) {
			def requestMatcher = new RequestMatcher(request, matchRules)
			def nextIndex = orderedIndex.getAndIncrement()
			def nextInteraction = interactions[nextIndex]
			if (!nextInteraction) {
				throw new IllegalStateException("No recording found at position $nextIndex")
			} else if (!requestMatcher.matches(nextInteraction.request)) {
				throw new IllegalStateException("Request ${stringify(request)} does not match recorded request ${stringify(nextInteraction.request)}")
			} else {
				nextInteraction.response
			}
		} else {
			int position = findMatch(request)
			if (position < 0) {
				throw new IllegalStateException('no matching recording found')
			} else {
				interactions[position].response
			}
		}
	}

	private String stringify(Request request) {
		[
			method: request.method,
			uri: request.uri,
			headers: request.headers,
			body: request.bodyAsText.text
		]
	}

	synchronized void record(Request request, Response response) {
		if (!mode.writable) {
			throw new IllegalStateException('the tape is not writable')
		}

		def interaction = new RecordedInteraction(
				request: recordRequest(request),
				response: recordResponse(response),
				recorded: new Date()
		)

		if (mode.sequential) {
			interactions << interaction

		} else {
			int position = findMatch(request)
			if (position >= 0) {
				interactions[position] = interaction
			} else {
				interactions << interaction
			}
		}
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private synchronized int findMatch(Request request) {
		def requestMatcher = new RequestMatcher(request, matchRules)
		interactions.findIndexOf {
			requestMatcher.matches(it.request)
		}
	}

	private static RecordedRequest recordRequest(Request request) {
		def clone = new RecordedRequest()
		clone.method = request.method
		clone.uri = request.uri
		request.headers.each {
			if (it.key != VIA) {
				clone.headers[it.key] = it.value
			}
		}
		clone.body = request.hasBody() ? request.bodyAsText.text : null // TODO: handle encoded / binary request bodies
		clone
	}

	private static RecordedResponse recordResponse(Response response) {
		def clone = new RecordedResponse()
		clone.status = response.status
		response.headers.each {
			if (!(it.key in [VIA, X_BETAMAX])) {
				clone.headers[it.key] = it.value
			}
		}
		if (response.hasBody()) {
			boolean representAsText = isTextContentType(response.contentType) && isPrintable(response.bodyAsText.text)
			clone.body = representAsText ? response.bodyAsText.text : response.bodyAsBinary.bytes
		}
		clone
	}

	static boolean isTextContentType(String contentType) {
		if (contentType) {
			contentType =~ /^text\/|application\/(json|javascript|(\w+\+)?xml)/
		} else {
			false
		}
	}

	static boolean isPrintable(String s) {
		// this check is performed by SnakeYaml but we need to do so *before* unzipping the byte stream otherwise we
		// won't be able to read it back again.
		!(s =~ StreamReader.NON_PRINTABLE)
	}

}

class RecordedInteraction {
	Date recorded
	RecordedRequest request
	RecordedResponse response
}


