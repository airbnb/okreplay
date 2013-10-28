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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.tape.*;
import co.freeside.betamax.tape.yaml.*;
import co.freeside.betamax.util.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;
import static co.freeside.betamax.MatchRules.*;

/**
 * This class is the main interface to Betamax. It allows control of Betamax configuration, inserting and
 * ejecting `Tape` instances and starting and stopping recording sessions.
 */
public class Recorder {

    public Recorder() {
        try {
            URL propertiesFile = Recorder.class.getResource("/betamax.properties");
            if (propertiesFile != null) {
                Properties properties = new Properties();
                properties.load(Files.newReader(new File(propertiesFile.getFile()), Charsets.UTF_8));
                configureFrom(properties);
            } else {
                configureWithDefaults();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Recorder(Properties properties) {
        configureFrom(properties);
    }

    public void start(String tapeName, Map arguments) {
        insertTape(tapeName, arguments);
    }

    public void start(String tapeName) {
        start(tapeName, new LinkedHashMap<Object, Object>());
    }

    public void stop() {
        ejectTape();
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
     *
     * @param name      the name of the _tape_.
     * @param arguments customize the behaviour of the tape.
     */
    @SuppressWarnings("unchecked")
    public void insertTape(String name, Map arguments) {
        tape = getTapeLoader().loadTape(name);
        if (tape instanceof MemoryTape) {
            MemoryTape memoryTape = (MemoryTape) tape;

            TapeMode mode = (TapeMode) arguments.get("mode");
            memoryTape.setMode(mode == null ? defaultMode : mode);

            List<Comparator<Request>> match = (List<Comparator<Request>>) arguments.get("match");
            Object[] array = match != null ? match.toArray() : null;
            MatchRule[] matchArray = array != null ? Arrays.copyOf(array, array.length, MatchRule[].class) : null;
            memoryTape.setMatchRules(matchArray != null ? matchArray : (MatchRule[]) Arrays.asList(method, uri).toArray());
        }
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
     *
     * @param name the name of the _tape_.
     */
    public void insertTape(String name) {
        insertTape(name, new LinkedHashMap<Object, Object>());
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

    protected void configureFrom(Properties properties) {
        tapeRoot = new File(properties.getProperty("betamax.tapeRoot", DEFAULT_TAPE_ROOT));
        defaultMode = TypedProperties.getEnum(properties, "betamax.defaultMode", TapeMode.READ_WRITE);
        final List<String> tokenize = Lists.newArrayList(Splitter.on(",").split((String) properties.getProperty("betamax.ignoreHosts")));
        ignoreHosts = tokenize != null ? tokenize : new ArrayList<String>();
        ignoreLocalhost = TypedProperties.getBoolean(properties, "betamax.ignoreLocalhost");
    }

    protected void configureWithDefaults() {
        tapeRoot = new File(DEFAULT_TAPE_ROOT);
        defaultMode = TapeMode.READ_WRITE;
        ignoreHosts = new ArrayList<String>();
        ignoreLocalhost = false;
    }

    /**
     * Not just a property as `tapeRoot` gets changed during constructor.
     */
    protected TapeLoader getTapeLoader() {
        return new YamlTapeLoader(tapeRoot);
    }

    public File getTapeRoot() {
        return tapeRoot;
    }

    public void setTapeRoot(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

    public TapeMode getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(TapeMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public Collection<String> getIgnoreHosts() {
        return ignoreHosts;
    }

    public void setIgnoreHosts(Collection<String> ignoreHosts) {
        this.ignoreHosts = ignoreHosts;
    }

    public boolean getIgnoreLocalhost() {
        return ignoreLocalhost;
    }

    public boolean isIgnoreLocalhost() {
        return ignoreLocalhost;
    }

    public void setIgnoreLocalhost(boolean ignoreLocalhost) {
        this.ignoreLocalhost = ignoreLocalhost;
    }

    public static final String DEFAULT_TAPE_ROOT = "src/test/resources/betamax/tapes";

    protected final Logger log = Logger.getLogger(getClass().getName());

    /**
     * The base directory where tape files are stored.
     */
    private File tapeRoot = new File(DEFAULT_TAPE_ROOT);

    /**
     * The default mode for an inserted tape.
     */
    private TapeMode defaultMode = TapeMode.READ_ONLY;

    /**
     * Hosts that are ignored by the proxy. Any connections made will be allowed to proceed normally and not be
     * intercepted.
     */
    private Collection<String> ignoreHosts = new ArrayList<String>();

    /**
     * If set to true all connections to localhost addresses are ignored.
     * This is equivalent to setting `ignoreHosts` to `['localhost', '127.0.0.1', InetAddress.localHost.hostName,
     * InetAddress.localHost.hostAddress]`.
     */
    private boolean ignoreLocalhost;

    private StorableTape tape;
}
