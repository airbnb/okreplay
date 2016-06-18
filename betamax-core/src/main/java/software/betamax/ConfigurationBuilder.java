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

package software.betamax;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import software.betamax.util.TypedProperties;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class ConfigurationBuilder {

    public Configuration build() {
        return new Configuration(this);
    }

    protected File tapeRoot = new File(Configuration.DEFAULT_TAPE_ROOT);
    protected TapeMode defaultMode = Configuration.DEFAULT_MODE;
    protected MatchRule defaultMatchRule = Configuration.DEFAULT_MATCH_RULE;
    protected ImmutableCollection<String> ignoreHosts = ImmutableList.of();
    protected boolean ignoreLocalhost;

    protected String proxyHost = Configuration.DEFAULT_PROXY_HOST;
    protected int proxyPort = Configuration.DEFAULT_PROXY_PORT;
    protected String proxyUser;
    protected String proxyPassword;
    protected int proxyTimeoutSeconds = Configuration.DEFAULT_PROXY_TIMEOUT;
    protected int requestBufferSize = Configuration.DEFAULT_REQUEST_BUFFER_SIZE;
    protected boolean sslEnabled;

    public ConfigurationBuilder withProperties(Properties properties) {
        if (properties.containsKey("betamax.tapeRoot")) {
            tapeRoot(new File(properties.getProperty("betamax.tapeRoot")));
        }

        if (properties.containsKey("betamax.defaultMode")) {
            defaultMode(TapeMode.valueOf(properties.getProperty("betamax.defaultMode")));
        }

        if (properties.containsKey("betamax.defaultMatchRules")) {
            List<MatchRule> rules = Lists.transform(Splitter.on(",").splitToList(properties.getProperty("betamax.defaultMatchRules")), new Function<String, MatchRule>() {
                @Override
                public MatchRule apply(String input) {
                    return MatchRules.valueOf(input);
                }
            });
            defaultMatchRule(ComposedMatchRule.of(rules));
        }

        if (properties.containsKey("betamax.ignoreHosts")) {
            ignoreHosts(Splitter.on(",").splitToList(properties.getProperty("betamax.ignoreHosts")));
        }

        if (properties.containsKey("betamax.ignoreLocalhost")) {
            ignoreLocalhost(Boolean.valueOf(properties.getProperty("betamax.ignoreLocalhost")));
        }

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

        return this;
    }

    public ConfigurationBuilder tapeRoot(File tapeRoot) {
        this.tapeRoot = tapeRoot;
        return this;
    }

    public ConfigurationBuilder defaultMode(TapeMode defaultMode) {
        this.defaultMode = defaultMode;
        return this;
    }

    public ConfigurationBuilder defaultMatchRule(MatchRule defaultMatchRule) {
        this.defaultMatchRule = defaultMatchRule;
        return this;
    }

    public ConfigurationBuilder defaultMatchRules(MatchRule... defaultMatchRules) {
        this.defaultMatchRule = ComposedMatchRule.of(defaultMatchRules);
        return this;
    }

    public ConfigurationBuilder ignoreHosts(Iterable<String> ignoreHosts) {
        this.ignoreHosts = ImmutableList.copyOf(ignoreHosts);
        return this;
    }

    public ConfigurationBuilder ignoreLocalhost(boolean ignoreLocalhost) {
        this.ignoreLocalhost = ignoreLocalhost;
        return this;
    }

    public ConfigurationBuilder proxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public ConfigurationBuilder proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public ConfigurationBuilder proxyAuth(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("The required proxy username and password cannot be null");
        }

        this.proxyUser = username;
        this.proxyPassword = password;
        return this;
    }

    public ConfigurationBuilder proxyTimeoutSeconds(int proxyTimeoutSeconds) {
        this.proxyTimeoutSeconds = proxyTimeoutSeconds;
        return this;
    }

    public ConfigurationBuilder requestBufferSize(int requestBufferSize){
        this.requestBufferSize = requestBufferSize;
        return this;
    }

    public ConfigurationBuilder sslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }
}
