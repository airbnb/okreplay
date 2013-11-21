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

import java.util.*;
import co.freeside.betamax.util.*;
import static co.freeside.betamax.ProxyConfiguration.*;

public abstract class ProxyConfigurationBuilder<T extends ProxyConfigurationBuilder<T>> extends ConfigurationBuilder<T> {


    public ProxyConfiguration build() {
        return new ProxyConfiguration(this);
    }

    protected String proxyHost = DEFAULT_PROXY_HOST;
    protected int proxyPort = DEFAULT_PROXY_PORT;
    protected int proxyTimeoutSeconds = DEFAULT_PROXY_TIMEOUT;
    protected int requestBufferSize = DEFAULT_REQUEST_BUFFER_SIZE;
    protected boolean sslEnabled;

    @Override
    public T withProperties(Properties properties) {
        super.withProperties(properties);

        if (properties.containsKey("betamax.proxyHost")) {
            proxyHost(properties.getProperty("betamax.proxyHost"));
        }

        if (properties.containsKey("betamax.proxyPort")) {
            proxyPort(TypedProperties.getInteger(properties, "betamax.proxyPort"));
        }

        if (properties.containsKey("betamax.proxyTimeoutSeconds")) {
            proxyTimeoutSeconds(TypedProperties.getInteger(properties, "betamax.proxyTimeoutSeconds"));
        }

        if (properties.containsKey("betamax.requestBufferSize")) {
            requestBufferSize(TypedProperties.getInteger(properties, "betamax.requestBufferSize"));
        }

        if (properties.containsKey("betamax.sslEnabled")) {
            sslEnabled(TypedProperties.getBoolean(properties, "betamax.sslEnabled"));
        }

        return self();
    }

    public T proxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return self();
    }

    public T proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return self();
    }

    public T proxyTimeoutSeconds(int proxyTimeoutSeconds) {
        this.proxyTimeoutSeconds = proxyTimeoutSeconds;
        return self();
    }

    public T requestBufferSize(int requestBufferSize){
        this.requestBufferSize = requestBufferSize;
        return self();
    }

    public T sslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return self();
    }
}
