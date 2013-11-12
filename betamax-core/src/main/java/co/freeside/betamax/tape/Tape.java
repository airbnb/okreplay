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
import co.freeside.betamax.handler.HandlerException;
import co.freeside.betamax.message.*;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
public interface Tape {

    /**
     * @return The name of the tape.
     */
    String getName();

    /**
     * @param mode the new record mode of the tape.
     */
    void setMode(TapeMode mode);

    TapeMode getMode();

    /**
     * @param matchRule the rules used to match recordings on the tape.
     */
    void setMatchRule(MatchRule matchRule);

    /**
     * @param entityStorage the policy used for storing response bodies on the
     *                      tape.
     */
    void setResponseBodyStorage(EntityStorage entityStorage);

    /**
     * @return `true` if the tape is readable, `false` otherwise.
     */
    boolean isReadable();

    /**
     * @return `true` if the tape is writable, `false` otherwise.
     */
    boolean isWritable();

    /**
     * @return `true` if access is sequential, `false` otherwise.
     */
    boolean isSequential();

    /**
     * @return the number of recorded HTTP interactions currently stored on the
     * tape.
     */
    int size();

    /**
     * Attempts to find a recorded interaction on the tape that matches the
     * supplied request.
     *
     * @param request the HTTP request to match.
     * @return `true` if a matching recorded interaction was found, `false`
     * otherwise.
     */
    boolean seek(Request request);

    /**
     * Retrieves a previously recorded response that matches the request.
     *
     * @param request the HTTP request to match.
     * @throws IllegalStateException if no matching recorded interaction
     *                               exists.
     */
    Response play(Request request) throws HandlerException;

    /**
     * Records a new interaction to the tape. If `request` matches an existing
     * interaction this method will overwrite
     * it. Otherwise the newly recorded interaction is appended to the tape.
     *
     * @param request  the request to record.
     * @param response the response to record.
     * @throws UnsupportedOperationException if this `Tape` implementation is
     *                                       not writable.
     */
    void record(Request request, Response response);

    /**
     * @return `true` if the tape content has changed since last being loaded
     * from disk, `false` otherwise.
     */
    boolean isDirty();
}
