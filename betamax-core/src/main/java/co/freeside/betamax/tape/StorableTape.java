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

import java.io.Writer;

/**
 * A `Tape` that can be read from an written to a backing store.
 */
public interface StorableTape extends Tape {
    /**
     * Writes the current state of the tape to `writer`.
     */
    public void writeTo(Writer writer);

    /**
     * @return `true` if the tape content has changed since last being loaded from disk, `false` otherwise.
     */
    public boolean isDirty();
}
