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

import co.freeside.betamax.ProxyConfiguration
import spock.lang.*
import static co.freeside.betamax.TapeMode.*

class ConfigurationSpec extends Specification {

    @Shared
    String tmpdir = System.properties."java.io.tmpdir"

    void "uses default configuration if not overridden and no properties file exists"() {
        given:
        def configuration = ProxyConfiguration.builder().build()

        expect:
        with(configuration) {
            tapeRoot == new File(DEFAULT_TAPE_ROOT)
            proxyPort == DEFAULT_PROXY_PORT
            defaultMode == DEFAULT_MODE
            proxyTimeoutSeconds == DEFAULT_PROXY_TIMEOUT
            ignoreHosts == []
            !ignoreLocalhost
            !sslEnabled
        }
    }

    void "configuration is overridden by builder methods"() {
        given:
        def configuration = ProxyConfiguration.builder()
                .tapeRoot(new File(tmpdir, "tapes"))
                .proxyPort(1337)
                .defaultMode(READ_ONLY)
                .proxyTimeoutSeconds(30)
                .ignoreHosts(["localhost"])
                .ignoreLocalhost(true)
                .sslEnabled(true)
                .build()


        expect:
        with(configuration) {
            tapeRoot == new File(tmpdir, "tapes")
            proxyPort == 1337
            defaultMode == READ_ONLY
            proxyTimeoutSeconds == 30
            ignoreHosts == ["localhost"]
            ignoreLocalhost
            sslEnabled
        }
    }

    void "configuration can be loaded from properties"() {
        given:
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", "${this.tmpdir}tapes".toString())
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }

        and:
        def configuration = ProxyConfiguration.builder().withProperties(properties).build()

        expect:
        with(configuration) {
            tapeRoot == new File(tmpdir, "tapes")
            proxyPort == 1337
            defaultMode == READ_WRITE
            proxyTimeoutSeconds == 30
            ignoreHosts == ["localhost", "127.0.0.1"]
            ignoreLocalhost
            sslEnabled
        }
    }

    void "default properties file is used if it exists"() {
        given:
        def propertiesFile = new File(tmpdir, "betamax.properties")
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", "${this.tmpdir}/tapes".toString())
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        ProxyConfiguration.classLoader.addURL(new File(tmpdir).toURL())

        and:
        def configuration = ProxyConfiguration.builder().build()

        expect:
        with(configuration) {
            tapeRoot == new File(tmpdir, "tapes")
            proxyPort == 1337
            defaultMode == READ_WRITE
            proxyTimeoutSeconds == 30
            ignoreHosts == ["localhost", "127.0.0.1"]
            ignoreLocalhost
            sslEnabled
        }

        cleanup:
        propertiesFile.delete()
    }

    void "builder methods override properties file"() {
        given:
        def propertiesFile = new File(tmpdir, "betamax.properties")
        def properties = new Properties()
        properties.with {
            setProperty("betamax.tapeRoot", "${this.tmpdir}/tapes".toString())
            setProperty("betamax.proxyPort", "1337")
            setProperty("betamax.defaultMode", "READ_WRITE")
            setProperty("betamax.proxyTimeoutSeconds", "30")
            setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
            setProperty("betamax.ignoreLocalhost", "true")
            setProperty("betamax.sslEnabled", "true")
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        ProxyConfiguration.classLoader.addURL(new File(tmpdir).toURL())

        and:
        def configuration = ProxyConfiguration.builder()
                .tapeRoot(new File("test/fixtures/tapes"))
                .proxyPort(1234)
                .defaultMode(WRITE_ONLY)
                .proxyTimeoutSeconds(10)
                .ignoreHosts(["github.com"])
                .ignoreLocalhost(false)
                .sslEnabled(false)
                .build()

        expect:
        with(configuration) {
            tapeRoot == new File("test/fixtures/tapes")
            proxyPort == 1234
            defaultMode == WRITE_ONLY
            proxyTimeoutSeconds == 10
            ignoreHosts == ["github.com"]
            !ignoreLocalhost
            !sslEnabled
        }

        cleanup:
        propertiesFile.delete()
    }
}
