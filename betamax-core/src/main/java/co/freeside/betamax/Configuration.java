/*
 * Copyright 2013 the original author or authors.
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
import java.util.*;
import co.freeside.betamax.internal.*;
import co.freeside.betamax.tape.*;
import co.freeside.betamax.util.*;
import com.google.common.collect.*;
import static co.freeside.betamax.MatchRules.method;
import static co.freeside.betamax.MatchRules.uri;

/**
 * The configuration used by Betamax.
 *
 * `Configuration` instances are created with a builder returned by the
 * {@link #builder()} factory method. For example:
 *
 * ```java
 * Configuration configuration = Configuration.builder()
 *                                            .tapeRoot(tapeRoot)
 *                                            .ignoreLocalhost(true)
 *                                            .build();
 * ```
 *
 * @see ConfigurationBuilder
 */
public class Configuration {

    public static final String DEFAULT_TAPE_ROOT = "src/test/resources/betamax/tapes";
    public static final TapeMode DEFAULT_MODE = TapeMode.READ_ONLY;
    public static final MatchRule DEFAULT_MATCH_RULE = ComposedMatchRule.of(method, uri);
    public static final EntityStorage DEFAULT_RESPONSE_BODY_STORAGE = EntityStorage.inline;

    private final File tapeRoot;
    private final TapeMode defaultMode;
    private final ImmutableCollection<String> ignoreHosts;
    private final boolean ignoreLocalhost;
    private final MatchRule defaultMatchRule;

    protected Configuration(ConfigurationBuilder<?> builder) {
        this.tapeRoot = builder.tapeRoot;
        this.defaultMode = builder.defaultMode;
        this.defaultMatchRule = builder.defaultMatchRule;
        this.ignoreHosts = builder.ignoreHosts;
        this.ignoreLocalhost = builder.ignoreLocalhost;
    }

    public static ConfigurationBuilder<?> builder() {
        return new Builder().configureFromPropertiesFile();
    }

    /**
     * The base directory where tape files are stored.
     */
    public File getTapeRoot() {
        return tapeRoot;
    }

    /**
     * The default mode for an inserted tape.
     */
    public TapeMode getDefaultMode() {
        return defaultMode;
    }

    public MatchRule getDefaultMatchRule() {
        return defaultMatchRule;
    }

    /**
     * Hosts that are ignored by Betamax. Any connections made will be allowed to proceed normally and not be
     * intercepted.
     */
    public Collection<String> getIgnoreHosts() {
        if (isIgnoreLocalhost()) {
            return new ImmutableSet.Builder<String>()
                    .addAll(ignoreHosts)
                    .addAll(Network.getLocalAddresses())
                    .build();
        } else {
            return ignoreHosts;
        }
    }

    /**
     * If `true` then all connections to localhost addresses are ignored.
     *
     * This is equivalent to including the following in the collection returned by {@link #getIgnoreHosts()}:
     * * `"localhost"`
     * * `"127.0.0.1"`
     * * `InetAddress.getLocalHost().getHostName()`
     * * `InetAddress.getLocalHost().getHostAddress()`
     */
    public boolean isIgnoreLocalhost() {
        return ignoreLocalhost;
    }

    /**
     * Called by the `Recorder` instance so that the configuration can add listeners.
     *
     * You should **not** call this method yourself.
     */
    public void registerListeners(Collection<RecorderListener> listeners) {
    }

    private static class Builder extends ConfigurationBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

}
