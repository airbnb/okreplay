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

import java.net.*;
import java.util.*;
import co.freeside.betamax.internal.*;
import co.freeside.betamax.proxy.*;

public class ProxyConfiguration extends Configuration {

    public static final int DEFAULT_PROXY_PORT = 5555;
    public static final int DEFAULT_PROXY_TIMEOUT = 5;

    private final int proxyPort;
    private final int proxyTimeoutSeconds;
    private final boolean sslEnabled;

    protected ProxyConfiguration(ProxyConfigurationBuilder<?> builder) {
        super(builder);
        this.proxyPort = builder.proxyPort;
        this.proxyTimeoutSeconds = builder.proxyTimeoutSeconds;
        this.sslEnabled = builder.sslEnabled;
    }

    public static ProxyConfigurationBuilder<?> builder() {
        return new Builder().configureFromPropertiesFile();
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
     * If set to true add support for proxying SSL (disable certificate checking).
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * @return the hostname or address where the proxy will run.
     */
    public String getProxyHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to get localhost address");
        }
    }

    /**
     * @return a `java.net.Proxy` instance configured to point to the Betamax proxy.
     */
    public Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
    }

    @Override
    public void registerListeners(Collection<RecorderListener> listeners) {
        listeners.add(new ProxyServer(this));
    }

    private static class Builder extends ProxyConfigurationBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }
}
