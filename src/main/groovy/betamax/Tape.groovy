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

package betamax

import betamax.proxy.*

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
interface Tape {

	/**
	 * @return The name of the tape.
	 */
	String getName()

	/**
	 * @param mode the new record mode of the tape.
	 */
	void setMode(TapeMode mode)

	/**
	 * @return `true` if the tape is readable, `false` otherwise.
	 */
	boolean isReadable()

	/**
	 * @return `true` if the tape is writable, `false` otherwise.
	 */
	boolean isWritable()

	/**
	 * @return the number of recorded HTTP interactions currently stored on the tape.
	 */
	int size()

	/**
	 * Attempts to find a recorded interaction on the tape that matches the supplied request's method and URI. If the
	 * method succeeds then subsequent calls to `play` will play back the response that was found.
	 * @param request the HTTP request to match.
	 * @return `true` if a matching recorded interaction was found, `false` otherwise.
	 */
	boolean seek(Request request)

	/**
	 * Resets the tape so that no recorded interaction is ready to play. Subsequent calls to `play` will throw
	 * `IllegalStateException` until a successful call to `seek` is made.
	 */
	void reset()

	/**
	 * Plays back a previously recorded interaction to the supplied response. Status, headers and entities are copied
	 * from the recorded interaction to `response`.
	 * @param response the HTTP response to populate.
	 * @throws IllegalStateException if no recorded interaction has been found by a previous call to `seek`.
	 */
	void play(Response response)

	/**
	 * Records a new interaction to the tape. If the tape is currently positioned to read a recorded interaction due to
	 * a previous successful `seek` call then this method will overwrite the existing recorded interaction. Otherwise
	 * the newly recorded interaction is appended to the tape.
	 * @param request the request to record.
	 * @param response the response to record.
	 * @throws UnsupportedOperationException if this `Tape` implementation is not writable.
	 */
	void record(Request request, Response response)
}
