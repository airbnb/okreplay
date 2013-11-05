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

import java.io.File;

/**
 * The interface for factories that load tapes from file storage.
 */
public interface TapeLoader<T extends Tape> {
    /**
     * Loads the named tape or returns a new blank tape if an existing tape cannot be located.
     *
     * @param name the name of the tape.
     * @return a tape loaded from a file or a new blank tape.
     */
    public T loadTape(String name);

    public void writeTape(Tape tape);

    /**
     * @return an appropriate file for storing a tape with the supplied name.
     */
    public File fileFor(String tapeName);
}
