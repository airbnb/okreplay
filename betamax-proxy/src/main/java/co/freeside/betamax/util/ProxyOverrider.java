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

package co.freeside.betamax.util;

import java.io.IOException;
import java.net.*;
import java.util.*;
import com.google.common.base.*;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.*;
import static java.net.Proxy.Type.HTTP;

/**
 * Provides a mechanism to temporarily override current HTTP and HTTPS proxy settings and restore them later.
 */
public class ProxyOverrider implements ChainedProxyManager {

    private final Map<String, InetSocketAddress> originalProxies = new LinkedHashMap<String, InetSocketAddress>();
    private final Collection<String> originalNonProxyHosts = new HashSet<String>();

    /**
     * Activates a proxy override for the given URI scheme.
     */
    public void activate(InetAddress host, int port, Collection<String> nonProxyHosts) {
        for (String scheme : new String[] {"http", "https"}) {
            String currentProxyHost = System.getProperty(scheme + ".proxyHost");
            String currentProxyPort = System.getProperty(scheme + ".proxyPort");
            if (currentProxyHost != null) {
                originalProxies.put(scheme, new InetSocketAddress(currentProxyHost, Integer.parseInt(currentProxyPort)));
            }
            System.setProperty(scheme + ".proxyHost", new InetSocketAddress(host, port).getHostString());
            System.setProperty(scheme + ".proxyPort", Integer.toString(port));
        }

        String currentNonProxyHosts = System.getProperty("http.nonProxyHosts");
        if (currentNonProxyHosts == null) {
            originalNonProxyHosts.clear();
        } else {
            for (String nonProxyHost : Splitter.on('|').split(currentNonProxyHosts)) {
                originalNonProxyHosts.add(nonProxyHost);
            }
        }
        System.setProperty("http.nonProxyHosts", Joiner.on('|').join(nonProxyHosts));
    }

    /**
     * Deactivates all proxy overrides restoring the pre-existing proxy settings if any.
     */
    public void deactivateAll() {
        for (String scheme : new String[] {"http", "https"}) {
            InetSocketAddress originalProxy = originalProxies.remove(scheme);
            if (originalProxy != null) {
                System.setProperty(scheme + ".proxyHost", originalProxy.getHostName());
                System.setProperty(scheme + ".proxyPort", Integer.toString(originalProxy.getPort()));
            } else {
                System.clearProperty(scheme + ".proxyHost");
                System.clearProperty(scheme + ".proxyPort");
            }
        }

        if (originalNonProxyHosts.isEmpty()) {
            System.clearProperty("http.nonProxyHosts");
        } else {
            System.setProperty("http.nonProxyHosts", Joiner.on('|').join(originalNonProxyHosts));
        }
        originalNonProxyHosts.clear();
    }

    /**
     * Used by the Betamax proxy so that it can use pre-existing proxy settings when forwarding requests that do not
     * match anything on tape.
     *
     * @return a proxy selector that uses the overridden proxy settings if any.
     */
    @Deprecated
    public ProxySelector getOriginalProxySelector() {
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                InetSocketAddress address = originalProxies.get(uri.getScheme());
                if (address != null && !(originalNonProxyHosts.contains(uri.getHost()))) {
                    return Collections.singletonList(new Proxy(HTTP, address));
                } else {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            }
        };
    }

    /**
     * Used by LittleProxy to connect to a downstream proxy if there is one.
     */
    @Override
    public void lookupChainedProxies(HttpRequest request, Queue<ChainedProxy> chainedProxies) {
        final InetSocketAddress originalProxy = originalProxies.get(URI.create(request.getUri()).getScheme());
        if (originalProxy != null) {
            ChainedProxy chainProxy = new ChainedProxyAdapter() {
                @Override
                public InetSocketAddress getChainedProxyAddress() {
                    return originalProxy;
                }
            };
            chainedProxies.add(chainProxy);
        }
    }

}
