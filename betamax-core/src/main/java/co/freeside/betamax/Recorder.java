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
import com.google.common.base.*;
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

    public void start(String tapeName, Optional<TapeMode> mode, Optional<Iterable<? extends MatchRule>> matchRules) {
        insertTape(tapeName, mode.orNull(), matchRules.orNull());
        for (RecorderListener listener : listeners) {
            listener.onRecorderStart(tape);
        }
    }

    public void start(String tapeName, TapeMode mode, Iterable<? extends MatchRule> matchRules) {
        start(tapeName, Optional.of(mode), Optional.<Iterable<? extends MatchRule>>of(matchRules));
    }

    public void start(String tapeName, TapeMode mode) {
        start(tapeName, Optional.of(mode), Optional.<Iterable<? extends MatchRule>>absent());
    }

    public void start(String tapeName) {
        start(tapeName, Optional.<TapeMode>absent(), Optional.<Iterable<? extends MatchRule>>absent());
    }

    public void stop() {
        for (RecorderListener listener : listeners) {
            listener.onRecorderStop();
        }
        ejectTape();
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file.
     *
     * @param name      the name of the _tape_.
     */
    public void insertTape(String name, TapeMode mode, Iterable<? extends MatchRule> matchRules) {
        tape = getTapeLoader().loadTape(name);
        tape.setMode(mode == null ? configuration.getDefaultMode() : mode);
        tape.setMatchRules(matchRules == null ? RequestMatcher.DEFAULT_RULES : matchRules);
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file.
     *
     * @param name the name of the _tape_.
     */
    public void insertTape(String name) {
        insertTape(name, null, null);
    }

    /**
     * Gets the current active _tape_.
     *
     * @return the active _tape_.
     */
    public Tape getTape() {
        return tape;
    }

    /**
     * 'Ejects' the current _tape_, writing its content to file. If the proxy is active after calling this method it
     * will no longer record or play back any HTTP traffic until another tape is inserted.
     */
    public void ejectTape() {
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
