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

package co.freeside.betamax.util

import io.netty.handler.codec.http.HttpRequest
import org.littleshoot.proxy.*
import static java.net.Proxy.Type.HTTP

/**
 * Provides a mechanism to temporarily override current HTTP and HTTPS proxy settings and restore them later.
 */
class ProxyOverrider implements ChainedProxyManager {

    private final Map<String, InetSocketAddress> originalProxies = [:]
    private final Collection<String> originalNonProxyHosts = [] as Set

    /**
     * Activates a proxy override for the given URI scheme.
     */
    void activate(String host, int port, Collection<String> nonProxyHosts) {
        for (scheme in ['http', 'https']) {
            def currentProxyHost = System.getProperty("${scheme}.proxyHost")
            def currentProxyPort = System.getProperty("${scheme}.proxyPort")
            if (currentProxyHost) {
                originalProxies[scheme] = new InetSocketAddress(currentProxyHost, currentProxyPort?.toInteger())
            }
            System.setProperty("${scheme}.proxyHost", host)
            System.setProperty("${scheme}.proxyPort", port.toString())
        }

        def currentNonProxyHosts = System.getProperty('http.nonProxyHosts')
        if (currentNonProxyHosts) {
            originalNonProxyHosts.addAll currentNonProxyHosts.tokenize('|')
        } else {
            originalNonProxyHosts.clear()
        }
        System.setProperty('http.nonProxyHosts', nonProxyHosts.join('|'))
    }

    /**
     * Deactivates all proxy overrides restoring the pre-existing proxy settings if any.
     */
    void deactivateAll() {
        for (scheme in ['http', 'https']) {
            def originalProxy = originalProxies.remove(scheme)
            if (originalProxy) {
                System.setProperty("${scheme}.proxyHost", originalProxy.hostName)
                System.setProperty("${scheme}.proxyPort", originalProxy.port.toString())
            } else {
                System.clearProperty("${scheme}.proxyHost")
                System.clearProperty("${scheme}.proxyPort")
            }
        }

        if (originalNonProxyHosts) {
            System.setProperty('http.nonProxyHosts', originalNonProxyHosts.join('|'))
        } else {
            System.clearProperty('http.nonProxyHosts')
        }
        originalNonProxyHosts.clear()
    }

    /**
     * Used by the Betamax proxy so that it can use pre-existing proxy settings when forwarding requests that do not
     * match anything on tape.
     *
     * @return a proxy selector that uses the overridden proxy settings if any.
     */
    @Deprecated
    ProxySelector getOriginalProxySelector() {
        new ProxySelector() {
            @Override
            List<Proxy> select(URI uri) {
                def address = originalProxies[uri.scheme]
                if (address && !(uri.host in originalNonProxyHosts)) {
                    [new Proxy(HTTP, address)]
                } else {
                    [Proxy.NO_PROXY]
                }
            }

            @Override
            void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
        }
    }

    /**
     * Used by LittleProxy to connect to a downstream proxy if there is one.
     */
    @Override
    void lookupChainedProxies(HttpRequest request, Queue<ChainedProxy> chainedProxies) {
        final InetSocketAddress originalProxy = originalProxies[request.uri.toURI().scheme]
        if (originalProxy) {
            def chainProxy = new ChainedProxyAdapter() {
                @Override
                InetSocketAddress getChainedProxyAddress() {
                    return originalProxy
                }
            }
            chainedProxies.add(chainProxy)
        }
    }

}
