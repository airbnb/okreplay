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

package co.freeside.betamax.proxy;

import java.net.*;
import java.util.logging.Logger;
import co.freeside.betamax.ProxyConfiguration;
import co.freeside.betamax.internal.RecorderListener;
import co.freeside.betamax.proxy.netty.PredicatedHttpFilters;
import co.freeside.betamax.tape.Tape;
import co.freeside.betamax.util.*;
import com.google.common.base.Predicate;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import static co.freeside.betamax.proxy.netty.PredicatedHttpFilters.httpMethodPredicate;
import static com.google.common.base.Predicates.not;
import static io.netty.handler.codec.http.HttpMethod.CONNECT;

public class ProxyServer implements RecorderListener {

    private final ProxyConfiguration configuration;
    private final ProxyOverrider proxyOverrider = new ProxyOverrider();
    private final SSLOverrider sslOverrider = new SSLOverrider();
    private HttpProxyServer proxyServer;
    private boolean running;

    private static final Predicate<HttpRequest> NOT_CONNECT = not(httpMethodPredicate(CONNECT));

    private static final Logger LOG = Logger.getLogger(ProxyServer.class.getName());

    public ProxyServer(ProxyConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onRecorderStart(Tape tape) {
        if (!isRunning()) {
            start(tape);
        }
    }

    @Override
    public void onRecorderStop() {
        if (isRunning()) {
            stop();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void start(final Tape tape) {
        if (isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already running");
        }

        InetSocketAddress address = new InetSocketAddress(configuration.getProxyHost(), configuration.getProxyPort());
        LOG.info(String.format("Betamax proxy is binding to %s", address));
        HttpProxyServerBootstrap proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withIdleConnectionTimeout(configuration.getProxyTimeoutSeconds())
                .withAddress(address);

        if (configuration.isSslEnabled()) {
            proxyServerBootstrap.withManInTheMiddle(new SelfSignedMitmManager());
        } else {
            proxyServerBootstrap.withChainProxyManager(proxyOverrider);
        }

        proxyServerBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return configuration.getRequestBufferSize();
            }

            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                HttpFilters filters = new BetamaxFilters(originalRequest, tape);
                return new PredicatedHttpFilters(filters, NOT_CONNECT, originalRequest);
            }
        });

        proxyServer = proxyServerBootstrap.start();
        running = true;

        overrideProxySettings();
        overrideSSLSettings();
    }

    public void stop() {
        if (!isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already stopped");
        }
        restoreOriginalProxySettings();
        restoreOriginalSSLSettings();

        proxyServer.stop();
        running = false;
    }

    private void overrideProxySettings() {
        proxyOverrider.activate(configuration.getProxyHost(), configuration.getProxyPort(), configuration.getIgnoreHosts());
    }

    private void restoreOriginalProxySettings() {
        proxyOverrider.deactivateAll();
    }

    private void overrideSSLSettings() {
        if (configuration.isSslEnabled()) {
            sslOverrider.activate();
        }
    }

    private void restoreOriginalSSLSettings() {
        if (configuration.isSslEnabled()) {
            sslOverrider.deactivate();
        }
    }

}

