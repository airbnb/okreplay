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

package co.freeside.betamax;

import java.util.*;
import co.freeside.betamax.internal.*;
import co.freeside.betamax.tape.*;
import co.freeside.betamax.tape.yaml.*;
import com.google.common.collect.*;

/**
 * This class is the main interface to Betamax. It controls the Betamax lifecycle, inserting and
 * ejecting {@link Tape} instances and starting and stopping recording sessions.
 */
public class Recorder {

    private final Configuration configuration;
    private final Collection<RecorderListener> listeners = Lists.newArrayList();

    public Recorder() {
        this(Configuration.builder().build());
    }

    public Recorder(Configuration configuration) {
        this.configuration = configuration;
        configuration.registerListeners(listeners);
    }

    public final void start(String tapeName, Map arguments) {
        insertTape(tapeName, arguments);
        for (RecorderListener listener : listeners) {
            listener.onRecorderStart(tape);
        }
    }

    public final void start(String tapeName) {
        start(tapeName, Collections.emptyMap());
    }

    public final void stop() {
        for (RecorderListener listener : listeners) {
            listener.onRecorderStop();
        }
        ejectTape();
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file.
     *
     * @param name      the name of the _tape_.
     * @param arguments customize the behaviour of the tape.
     */
    @SuppressWarnings("unchecked")
    public final void insertTape(String name, Map arguments) {
        tape = getTapeLoader().loadTape(name);

        if (arguments.containsKey("mode")) {
            tape.setMode((TapeMode) arguments.get("mode"));
        } else {
            tape.setMode(configuration.getDefaultMode());
        }

        if (arguments.containsKey("match")) {
            tape.setMatchRules((List<MatchRule>) arguments.get("match"));
        }
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file.
     *
     * @param name the name of the _tape_.
     */
    public final void insertTape(String name) {
        insertTape(name, new LinkedHashMap<Object, Object>());
    }

    /**
     * Gets the current active _tape_.
     *
     * @return the active _tape_.
     */
    public Tape getTape() {
        return tape;
    }// TODO: this should be final but a couple of tests mock it

    /**
     * 'Ejects' the current _tape_, writing its content to file. If the proxy is active after calling this method it
     * will no longer record or play back any HTTP traffic until another tape is inserted.
     */
    public final void ejectTape() {
        if (tape != null) {
            getTapeLoader().writeTape(tape);
            tape = null;
        }
    }

    /**
     * Not just a property as `tapeRoot` gets changed during constructor.
     */
    private TapeLoader getTapeLoader() {
        return new YamlTapeLoader(configuration.getTapeRoot());
    }

    private StorableTape tape;
}
