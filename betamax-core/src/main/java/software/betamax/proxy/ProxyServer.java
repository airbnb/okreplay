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

package software.betamax.proxy;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.io.FileUtils;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.betamax.Configuration;
import software.betamax.internal.RecorderListener;
import software.betamax.proxy.netty.PredicatedHttpFilters;
import software.betamax.tape.Tape;
import software.betamax.util.ProxyOverrider;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import static com.google.common.base.Predicates.not;
import static io.netty.handler.codec.http.HttpMethod.CONNECT;
import static software.betamax.proxy.netty.PredicatedHttpFilters.httpMethodPredicate;

public class ProxyServer implements RecorderListener {

    private final Configuration configuration;
    private final ProxyOverrider proxyOverrider = new ProxyOverrider();
    private HttpProxyServer proxyServer;
    private boolean running;

    private static final Predicate<HttpRequest> NOT_CONNECT = not(httpMethodPredicate(CONNECT));

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServer.class.getName());

    public ProxyServer(Configuration configuration) {
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
                .withAddress(address)
                .withTransparent(true);

        if (configuration.isSslEnabled()) {
            proxyServerBootstrap.withManInTheMiddle(createMitmManager());
        } else {
            proxyServerBootstrap.withChainProxyManager(proxyOverrider);
        }

        if (configuration.getProxyUser() != null) {
            proxyServerBootstrap.withProxyAuthenticator(new ProxyAuthenticator() {
                @Override
                public String getRealm() {
                    return null;
                }

                @Override
                public boolean authenticate(String userName, String password) {
                    return configuration.getProxyUser().equals(userName)
                            && configuration.getProxyPassword().equals(password);
                }
            });
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
    }

    public void stop() {
        if (!isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already stopped");
        }
        restoreOriginalProxySettings();

        proxyServer.stop();
        running = false;
    }

    private void overrideProxySettings() {
        proxyOverrider.activate(configuration.getProxyHost(), configuration.getProxyPort(), configuration.getIgnoreHosts());
    }

    private void restoreOriginalProxySettings() {
        proxyOverrider.deactivateAll();
    }

    private MitmManager createMitmManager() {
        try {

            // Use the same betamax private key & cert for backwards compatibility with 2.0.1
            // We use temporary files here so we don't pollute people's projects with temporary (and fake) keys and certs
            File tempSSLDir = Files.createTempDir();

            Path storePath = tempSSLDir.toPath().resolve("betamax.p12");
            InputStream betamaxKeystoreStream = getClass().getClassLoader().getResourceAsStream("betamax.p12");
            FileUtils.copyInputStreamToFile(betamaxKeystoreStream, storePath.toFile());

            Path keyPath = tempSSLDir.toPath().resolve("betamax.pem");
            InputStream betamaxKeyStream = getClass().getClassLoader().getResourceAsStream("betamax.pem");
            FileUtils.copyInputStreamToFile(betamaxKeyStream, keyPath.toFile());

            Authority authority = new Authority(
                    tempSSLDir,
                    "betamax",
                    "changeit".toCharArray(),
                    "betamax.software",
                    "Betamax",
                    "Certificate Authority",
                    "Betamax",
                    "betamax.software"
            );

            return new CertificateSniffingMitmManager(authority);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create MITMManager", ex);
        }
    }
}

