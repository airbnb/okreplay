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

package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.netty.PredicatedHttpFilters
import co.freeside.betamax.util.*
import com.google.common.base.Predicate
import io.netty.handler.codec.http.HttpRequest
import org.littleshoot.proxy.*
import org.littleshoot.proxy.extras.SelfSignedMitmManager
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import static co.freeside.betamax.proxy.netty.PredicatedHttpFilters.httpMethodPredicate
import static com.google.common.base.Predicates.not
import static io.netty.handler.codec.http.HttpMethod.CONNECT
import static java.util.concurrent.TimeUnit.MILLISECONDS

class ProxyServer implements HttpInterceptor {

    private final HttpProxyServerBootstrap proxyServerBootstrap;
    private final ProxyRecorder recorder
    private final ProxyOverrider proxyOverrider = new ProxyOverrider()
    private final SSLOverrider sslOverrider = new SSLOverrider()
    private HttpProxyServer proxyServer;
    private boolean running = false
    private final InetSocketAddress address

    private static final Predicate<HttpRequest> NOT_CONNECT = not(httpMethodPredicate(CONNECT))

    ProxyServer(ProxyRecorder recorder) {
        this.recorder = recorder

        address = new InetSocketAddress(InetAddress.getLocalHost(), recorder.getProxyPort());
//		address = new InetSocketAddress(NetworkUtils.getLocalHost(), recorder.getProxyPort());
        println "created address, $address"
        proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withIdleConnectionTimeout(MILLISECONDS.toSeconds(recorder.proxyTimeout) as int)
                .withAddress(address)

        if (recorder.sslSupport) {
            proxyServerBootstrap.withManInTheMiddle(new SelfSignedMitmManager())
        } else {
            proxyServerBootstrap.withChainProxyManager(proxyOverrider)
        }

        proxyServerBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            HttpFilters filterRequest(HttpRequest originalRequest) {
                def filters = new BetamaxFilters(originalRequest, recorder.tape)
                return new PredicatedHttpFilters(filters, NOT_CONNECT, originalRequest);
            }
        })
    }

    @Override
    boolean isRunning() {
        running
    }

    void start() {
        if (isRunning()) throw new IllegalStateException("Betamax proxy server is already running")
        proxyServer = proxyServerBootstrap.start()
        running = true

        overrideProxySettings()
        overrideSSLSettings()
    }

    @Override
    void stop() {
        if (!isRunning()) throw new IllegalStateException("Betamax proxy server is already stopped")
        restoreOriginalProxySettings()
        restoreOriginalSSLSettings()

        proxyServer.stop()
        running = false
    }

    @Override
    String getHost() {
        address.hostName
    }

    @Override
    int getPort() {
        address.port
    }

    private void overrideProxySettings() {
        def nonProxyHosts = recorder.ignoreHosts as Set
        if (recorder.ignoreLocalhost) {
            nonProxyHosts.addAll(Network.localAddresses)
        }
        proxyOverrider.activate address.hostName, address.port, nonProxyHosts
    }

    private void restoreOriginalProxySettings() {
        proxyOverrider.deactivateAll()
    }

    private void overrideSSLSettings() {
        if (recorder.sslSupport) {
            sslOverrider.activate()
        }
    }

    private void restoreOriginalSSLSettings() {
        if (recorder.sslSupport) {
            sslOverrider.deactivate()
        }
    }

}

