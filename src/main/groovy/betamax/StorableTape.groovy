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

/**
 * A `Tape` that can be read from an written to a backing store.
 */
interface StorableTape extends Tape {

	/**
	 * Writes the current state of the tape to `writer`.
	 */
	void writeTo(Writer writer)

	/**
	 * Reads the state of the tape from `writer` discarding any existing state.
	 */
	void readFrom(Reader reader)

	/**
	 * @return an appropriate filename for storing the tape to the filesystem.
	 */
	String getFilename()

}