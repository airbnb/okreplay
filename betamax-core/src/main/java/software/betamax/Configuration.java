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

package software.betamax;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import software.betamax.internal.RecorderListener;
import software.betamax.proxy.ProxyConfigurationException;
import software.betamax.proxy.ProxyServer;
import software.betamax.util.Network;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.Properties;

/**
 * The configuration used by Betamax.
 *
 * `Configuration` instances are created with a builder returned by the
 * {@link #builder()} factory method. For example:
 *
 * [source,java]
 * ----
 * Configuration configuration = Configuration.builder()
 *                                            .tapeRoot(tapeRoot)
 *                                            .ignoreLocalhost(true)
 *                                            .build();
 * ----
 *
 * @see ConfigurationBuilder
 */
public class Configuration {

    public static final String DEFAULT_TAPE_ROOT = "src/test/resources/betamax/tapes";
    public static final TapeMode DEFAULT_MODE = TapeMode.READ_ONLY;
    public static final MatchRule DEFAULT_MATCH_RULE = ComposedMatchRule.of(MatchRules.method, MatchRules.uri);

    public static final String DEFAULT_PROXY_HOST = "0.0.0.0";
    public static final int DEFAULT_REQUEST_BUFFER_SIZE = 8388608; //8MB
    public static final int DEFAULT_PROXY_PORT = 5555;
    public static final int DEFAULT_PROXY_TIMEOUT = 5;

    private final File tapeRoot;
    private final TapeMode defaultMode;
    private final ImmutableCollection<String> ignoreHosts;
    private final boolean ignoreLocalhost;
    private final MatchRule defaultMatchRule;

    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUser;
    private final String proxyPassword;
    private final int proxyTimeoutSeconds;
    private final boolean sslEnabled;
    private final int requestBufferSize;

    protected Configuration(ConfigurationBuilder builder) {
        this.tapeRoot = builder.tapeRoot;
        this.defaultMode = builder.defaultMode;
        this.defaultMatchRule = builder.defaultMatchRule;
        this.ignoreHosts = builder.ignoreHosts;
        this.ignoreLocalhost = builder.ignoreLocalhost;
        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
        this.proxyUser = builder.proxyUser;
        this.proxyPassword = builder.proxyPassword;
        this.proxyTimeoutSeconds = builder.proxyTimeoutSeconds;
        this.sslEnabled = builder.sslEnabled;
        this.requestBufferSize = builder.requestBufferSize;
    }

    public static ConfigurationBuilder builder() {
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();

            URL propertiesFile = Configuration.class.getResource("/betamax.properties");
            if (propertiesFile != null) {
                Properties properties = new Properties();
                properties.load(propertiesFile.openStream());
                return builder.withProperties(properties);
            } else {
                return builder;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * The port the Betamax proxy will listen on.
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * The time (in seconds) the proxy will wait before aborting a request.
     */
    public int getProxyTimeoutSeconds() {
        return proxyTimeoutSeconds;
    }

    /**
     * The buffer size the proxy will use to aggregate incoming requests.
     * Needed if you want to match on request body.
     */
    public int getRequestBufferSize() {
        return requestBufferSize;
    }

    /**
     * If set to true add support for proxying SSL (disable certificate
     * checking).
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * @return the hostname or address where the proxy will run. A value of
     * `null` means the proxy will bind to any local address.
     * @see java.net.InetSocketAddress#InetSocketAddress(InetAddress, int)
     */
    public InetAddress getProxyHost() {
        try {
            if (proxyHost == null) {
                return InetAddress.getByName(DEFAULT_PROXY_HOST);
            } else {
                return InetAddress.getByName(proxyHost);
            }
        } catch (UnknownHostException e) {
            throw new ProxyConfigurationException(String.format("Unable to resolve host %s", proxyHost), e);
        }
    }

    /**
     * The username required to authenticate with the proxy.
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * The password required to authenticate with the proxy.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * @return a `java.net.Proxy` instance configured to point to the Betamax
     * proxy.
     */
    public Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
    }

    /**
     * Called by the `Recorder` instance so that the configuration can add listeners.
     *
     * You should **not** call this method yourself.
     */
    public void registerListeners(Collection<RecorderListener> listeners) {
        listeners.add(new ProxyServer(this));
    }
}
