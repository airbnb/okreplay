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

package co.freeside.betamax.recorder

import co.freeside.betamax.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.*

class ConfigurationSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tempDir = Files.createTempDir()

    void "uses default configuration if not overridden and no properties file exists"() {
        given:
        def configuration = ProxyConfiguration.builder().build()

        expect:
        with(configuration) {
            tapeRoot == new File(DEFAULT_TAPE_ROOT)
            proxyHost == InetAddress.getByName(DEFAULT_PROXY_HOST)
            proxyPort == DEFAULT_PROXY_PORT
            defaultMode == DEFAULT_MODE
            defaultMatchRule == DEFAULT_MATCH_RULE
            proxyTimeoutSeconds == DEFAULT_PROXY_TIMEOUT
            ignoreHosts == []
            !ignoreLocalhost
            !sslEnabled
        }
    }

    void "configuration is overridden by builder methods"() {
        given:
        def configuration = ProxyConfiguration.builder()
                .tapeRoot(tempDir)
                .proxyHost("freeside.co")
                .proxyPort(1337)
                .defaultMode(READ_ONLY)
                .defaultMatchRules(MatchRules.host, MatchRules.headers)
                .proxyTimeoutSeconds(30)
                .ignoreHosts(["freeside.co"])
                .ignoreLocalhost(true)
                .sslEnabled(true)
                .build()


        expect:
        with(configuration) {
            tapeRoot == tempDir
            proxyHost == InetAddress.getByName("freeside.co")
            proxyPort == 1337
            defaultMode == READ_ONLY
            defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.headers)
            proxyTimeoutSeconds == 30
            ignoreHosts.contains("freeside.co")
            ignoreLocalhost
            sslEnabled
        }
    }

    void "configuration can be loaded from properties"() {
        given:
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
            setProperty("betamax.proxyHost", "freeside.co")
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.defaultMatchRules", "host,headers")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "freeside.co,energizedwork.com")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }

        and:
        def configuration = ProxyConfiguration.builder().withProperties(properties).build()

        expect:
        with(configuration) {
            tapeRoot == tempDir
            proxyHost == InetAddress.getByName("freeside.co")
            proxyPort == 1337
            defaultMode == READ_WRITE
            defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.headers)
            proxyTimeoutSeconds == 30
            ignoreHosts.contains("freeside.co")
            ignoreHosts.contains("energizedwork.com")
            ignoreLocalhost
            sslEnabled
        }
    }

    void "default properties file is used if it exists"() {
        given:
        def propertiesFile = new File(tempDir, "betamax.properties")
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
            setProperty("betamax.proxyHost", "freeside.co")
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.defaultMatchRules", "host,headers")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "freeside.co,energizedwork.com")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        ProxyConfiguration.classLoader.addURL(tempDir.toURL())

        and:
        def configuration = ProxyConfiguration.builder().build()

        expect:
        with(configuration) {
            tapeRoot == tempDir
            proxyHost == InetAddress.getByName("freeside.co")
            proxyPort == 1337
            defaultMode == READ_WRITE
            defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.headers)
            proxyTimeoutSeconds == 30
            ignoreHosts.contains("freeside.co")
            ignoreHosts.contains("energizedwork.com")
            ignoreLocalhost
            sslEnabled
        }

        cleanup:
        propertiesFile.delete()
    }

    void "builder methods override properties file"() {
        given:
        def propertiesFile = new File(tempDir, "betamax.properties")
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
            setProperty("betamax.proxyHost", "freeside.co")
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.defaultMatchRules", "host,headers")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        ProxyConfiguration.classLoader.addURL(tempDir.toURL())

        and:
        def configuration = ProxyConfiguration.builder()
                .tapeRoot(new File("test/fixtures/tapes"))
                .proxyHost("betamax.io")
                .proxyPort(1234)
                .defaultMode(WRITE_ONLY)
                .defaultMatchRules(MatchRules.port, MatchRules.query)
                .proxyTimeoutSeconds(10)
                .ignoreHosts(["github.com"])
                .ignoreLocalhost(false)
                .sslEnabled(false)
                .build()

        expect:
        with(configuration) {
            tapeRoot == new File("test/fixtures/tapes")
            proxyHost == InetAddress.getByName("betamax.io")
            proxyPort == 1234
            defaultMode == WRITE_ONLY
            defaultMatchRule == ComposedMatchRule.of(MatchRules.port, MatchRules.query)
            proxyTimeoutSeconds == 10
            ignoreHosts == ["github.com"]
            !ignoreLocalhost
            !sslEnabled
        }

        cleanup:
        propertiesFile.delete()
    }
}
