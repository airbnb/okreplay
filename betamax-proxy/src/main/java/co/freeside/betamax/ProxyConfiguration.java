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
import java.util.Collection;
import co.freeside.betamax.internal.RecorderListener;
import co.freeside.betamax.proxy.*;

public class ProxyConfiguration extends Configuration {

    public static final String DEFAULT_PROXY_HOST = "0.0.0.0";
    public static final int DEFAULT_REQUEST_BUFFER_SIZE = 8388608; //8MB
    public static final int DEFAULT_PROXY_PORT = 5555;
    public static final int DEFAULT_PROXY_TIMEOUT = 5;

    private final String proxyHost;
    private final int proxyPort;
    private final int proxyTimeoutSeconds;
    private final boolean sslEnabled;
    private final int requestBufferSize;

    protected ProxyConfiguration(ProxyConfigurationBuilder<?> builder) {
        super(builder);
        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
        this.proxyTimeoutSeconds = builder.proxyTimeoutSeconds;
        this.sslEnabled = builder.sslEnabled;
        this.requestBufferSize = builder.requestBufferSize;
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
     * @return a `java.net.Proxy` instance configured to point to the Betamax
     * proxy.
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
