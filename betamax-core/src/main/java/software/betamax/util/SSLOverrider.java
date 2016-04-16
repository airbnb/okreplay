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

package software.betamax.util;

import software.betamax.proxy.ssl.DummyJVMSSLSocketFactory;

import java.security.Security;

public class SSLOverrider {

    public static final String SSL_SOCKET_FACTORY_PROVIDER = "ssl.SocketFactory.provider";

    private boolean isActive;
    private String originalSocketFactoryProvider;

    public void activate() {
        if (!isActive) {
            originalSocketFactoryProvider = Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER);
            Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummyJVMSSLSocketFactory.class.getName());
        }

        isActive = true;
    }

    public void deactivate() {
        if (isActive) {
            Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, originalSocketFactoryProvider != null ? originalSocketFactoryProvider : "");
        }

        isActive = false;
    }

}
