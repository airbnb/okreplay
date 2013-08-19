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

package co.freeside.betamax.handler;

import co.freeside.betamax.Recorder;
import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;
import co.freeside.betamax.tape.Tape;
import static co.freeside.betamax.Headers.X_BETAMAX;

import java.util.logging.Logger;

/**
 * Reads the tape to find a matching exchange, returning the response if found otherwise proceeding the request, storing
 * & returning the new response.
 */
public class TapeReader extends ChainedHttpHandler {
    public TapeReader(Recorder recorder) {
        this.recorder = recorder;
    }

    public Response handle(Request request) {
        Tape tape = recorder.getTape();

        if (tape == null)
            throw new NoTapeException();

        if (tape.isReadable() && tape.seek(request)) {
            log.info("Playing back from '" + tape.getName() + "'");
            Response response = tape.play(request);
            response.addHeader(X_BETAMAX, "PLAY");
            return response;
        }

        if (tape.isWritable())
            return chain(request);

        throw new NonWritableTapeException();
    }

    private final Recorder recorder;
    private static final Logger log = Logger.getLogger(TapeReader.class.getName());
}
