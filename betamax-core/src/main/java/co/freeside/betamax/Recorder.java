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

    /**
     * Starts the Recorder, inserting a tape with the specified parameters.
     *
     * @param tapeName the name of the tape.
     * @param mode the tape mode. If not supplied the default mode from the configuration is used.
     * @param matchRule the rules used to match recordings on the tape. If not supplied a default is used.
     *
     * @throws IllegalStateException if the Recorder is already started.
     */
    public void start(String tapeName, Optional<TapeMode> mode, Optional<MatchRule> matchRule) {
        if (tape != null) {
            throw new IllegalStateException("start called when Recorder is already started");
        }

        tape = getTapeLoader().loadTape(tapeName);
        tape.setMode(mode.or(configuration.getDefaultMode()));
        tape.setMatchRule(matchRule.or(configuration.getDefaultMatchRule()));

        for (RecorderListener listener : listeners) {
            listener.onRecorderStart(tape);
        }
    }

    public void start(String tapeName, TapeMode mode, MatchRule matchRule) {
        start(tapeName, mode.toOptional(), Optional.<MatchRule>of(matchRule));
    }

    public void start(String tapeName, TapeMode mode) {
        start(tapeName, mode.toOptional(), Optional.<MatchRule>absent());
    }

    public void start(String tapeName) {
        start(tapeName, Optional.<TapeMode>absent(), Optional.<MatchRule>absent());
    }

    /**
     * Stops the Recorder and writes its current tape out to a file.
     *
     * @throws IllegalStateException if the Recorder is not started.
     */
    public void stop() {
        if (tape == null) {
            throw new IllegalStateException("stop called when Recorder is not started");
        }

        for (RecorderListener listener : listeners) {
            listener.onRecorderStop();
        }
        getTapeLoader().writeTape(tape);
        tape = null;
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
     * Not just a property as `tapeRoot` gets changed during constructor.
     */
    private TapeLoader<? extends Tape> getTapeLoader() {
        return new YamlTapeLoader(configuration.getTapeRoot());
    }

    private Tape tape;
}
