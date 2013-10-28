/*
 * Copyright 2012 the original author or authors.
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
import java.security.*;
import java.util.*;
import co.freeside.betamax.proxy.*;
import co.freeside.betamax.proxy.ssl.*;
import org.apache.http.conn.ssl.*;
import static co.freeside.betamax.util.TypedProperties.*;

public class ProxyRecorder extends Recorder {

    public static final int DEFAULT_PROXY_PORT = 5555;
    public static final int DEFAULT_PROXY_TIMEOUT = 5;
    public static final SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY;

    static {
        try {
            DEFAULT_SSL_SOCKET_FACTORY = DummySSLSocketFactory.getInstance();
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The port the Betamax proxy will listen on.
     */
    private int proxyPort;

    /**
     * The time (in seconds) the proxy will wait before aborting a request.
     */
    private int proxyTimeoutSeconds;

    /**
     * If set to true add support for proxying SSL (disable certificate checking).
     */
    private boolean sslSupport;

    /**
     * The factory that will be used to create SSL sockets for secure connections to the target.
     */
    private SSLSocketFactory sslSocketFactory;

    private ProxyServer interceptor;

    public ProxyRecorder() {
        super();
    }

    public ProxyRecorder(Properties properties) {
        super(properties);
    }

    /**
     * @return the hostname or address where the proxy will run.
     */
    public String getProxyHost() {
        return interceptor.getHost();
    }

    /**
     * @return a `java.net.Proxy` instance configured to point to the Betamax proxy.
     */
    public Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(interceptor.getHost(), interceptor.getPort()));
    }

    @Override
    public void start(String tapeName, Map arguments) {
        if (interceptor == null) {
            try {
                interceptor = new ProxyServer(this);
            } catch (UnknownHostException e) {
                throw new RuntimeException("Unable to start proxy", e);
            }
        }
        if (!interceptor.isRunning()) {
            interceptor.start();
        }
        super.start(tapeName, arguments);
    }

    @Override
    public void stop() {
        interceptor.stop();
        super.stop();
    }

    @Override
    protected void configureFrom(Properties properties) {
        super.configureFrom(properties);

        proxyPort = getInteger(properties, "betamax.proxyPort", DEFAULT_PROXY_PORT);
        proxyTimeoutSeconds = getInteger(properties, "betamax.proxyTimeout", DEFAULT_PROXY_TIMEOUT);
        sslSupport = getBoolean(properties, "betamax.sslSupport");
    }

    @Override
    protected void configureWithDefaults() {
        super.configureWithDefaults();

        proxyPort = DEFAULT_PROXY_PORT;
        proxyTimeoutSeconds = DEFAULT_PROXY_TIMEOUT;
        sslSupport = false;
        sslSocketFactory = DEFAULT_SSL_SOCKET_FACTORY;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public int getProxyTimeoutSeconds() {
        return proxyTimeoutSeconds;
    }

    public void setProxyTimeoutSeconds(int proxyTimeoutSeconds) {
        this.proxyTimeoutSeconds = proxyTimeoutSeconds;
    }

    public boolean isSslSupport() {
        return sslSupport;
    }

    public void setSslSupport(boolean sslSupport) {
        this.sslSupport = sslSupport;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }
}
