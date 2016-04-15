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

package software.betamax.util;

import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.SslEngineSource;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.util.HashMap;

/**
 * Created by sean on 3/22/16.
 */
public class BetamaxMitmManager implements MitmManager {

    private HashMap<String, SslEngineSource> sslEngineSources = new HashMap<>();

    @Override
    public SSLEngine serverSslEngine(String peerHost, int peerPort) {
        if (!sslEngineSources.containsKey(peerHost)) {
            sslEngineSources.put(peerHost, new DynamicSelfSignedSslEngineSource(peerHost, peerPort));
        }
        return sslEngineSources.get(peerHost).newSslEngine();
    }

    @Override
    public SSLEngine clientSslEngineFor(SSLSession serverSslSession) {
        return sslEngineSources.get(serverSslSession.getPeerHost()).newSslEngine();
    }
}
