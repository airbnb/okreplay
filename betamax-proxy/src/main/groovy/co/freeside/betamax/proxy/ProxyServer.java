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
import java.util.*;
import java.util.logging.*;
import co.freeside.betamax.*;
import co.freeside.betamax.proxy.netty.*;
import co.freeside.betamax.util.*;
import com.google.common.base.*;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.extras.*;
import org.littleshoot.proxy.impl.*;
import static co.freeside.betamax.proxy.netty.PredicatedHttpFilters.*;
import static com.google.common.base.Predicates.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static java.util.concurrent.TimeUnit.*;

public class ProxyServer implements HttpInterceptor {

    private final HttpProxyServerBootstrap proxyServerBootstrap;
    private final ProxyRecorder recorder;
    private final ProxyOverrider proxyOverrider = new ProxyOverrider();
    private final SSLOverrider sslOverrider = new SSLOverrider();
    private HttpProxyServer proxyServer;
    private boolean running;
    private final InetSocketAddress address;

    private static final Predicate<HttpRequest> NOT_CONNECT = not(httpMethodPredicate(CONNECT));

    private static final Logger LOG = Logger.getLogger(ProxyServer.class.getName());

    public ProxyServer(final ProxyRecorder recorder) throws UnknownHostException {
        this.recorder = recorder;

        address = new InetSocketAddress(InetAddress.getLocalHost(), recorder.getProxyPort());
//        address = new InetSocketAddress(NetworkUtils.getLocalHost(), recorder.getProxyPort());
        LOG.info(String.format("created address, %s", address));
        proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withIdleConnectionTimeout((int) MILLISECONDS.toSeconds(recorder.getProxyTimeout()))
                .withAddress(address);

        if (recorder.getSslSupport()) {
            proxyServerBootstrap.withManInTheMiddle(new SelfSignedMitmManager());
        } else {
            proxyServerBootstrap.withChainProxyManager(proxyOverrider);
        }

        proxyServerBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                HttpFilters filters = new BetamaxFilters(originalRequest, recorder.getTape());
                return new PredicatedHttpFilters(filters, NOT_CONNECT, originalRequest);
            }
        });
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already running");
        }
        proxyServer = proxyServerBootstrap.start();
        running = true;

        overrideProxySettings();
        overrideSSLSettings();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already stopped");
        }
        restoreOriginalProxySettings();
        restoreOriginalSSLSettings();

        proxyServer.stop();
        running = false;
    }

    @Override
    public String getHost() {
        return address.getHostName();
    }

    @Override
    public int getPort() {
        return address.getPort();
    }

    private void overrideProxySettings() {
        Collection<String> nonProxyHosts = recorder.getIgnoreHosts();
        if (recorder.getIgnoreLocalhost()) {
            nonProxyHosts.addAll(Network.getLocalAddresses());
        }
        proxyOverrider.activate(address.getHostName(), address.getPort(), nonProxyHosts);
    }

    private void restoreOriginalProxySettings() {
        proxyOverrider.deactivateAll();
    }

    private void overrideSSLSettings() {
        if (recorder.getSslSupport()) {
            sslOverrider.activate();
        }
    }

    private void restoreOriginalSSLSettings() {
        if (recorder.getSslSupport()) {
            sslOverrider.deactivate();
        }
    }

}

