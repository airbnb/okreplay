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
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import spock.lang.*
import static ProxyRecorder.DEFAULT_PROXY_TIMEOUT
import static co.freeside.betamax.Recorder.DEFAULT_TAPE_ROOT
import static co.freeside.betamax.TapeMode.*

class RecorderConfigurationSpec extends Specification {

    @Shared
    String tmpdir = System.properties.'java.io.tmpdir'

    void 'recorder gets default configuration if not overridden and no properties file exists'() {
        given:
        def recorder = new ProxyRecorder()

        expect:
        with(recorder) {
            tapeRoot == new File(DEFAULT_TAPE_ROOT)
            proxyPort == 5555
            defaultMode == READ_WRITE
            proxyTimeout == DEFAULT_PROXY_TIMEOUT
            ignoreHosts == []
            !ignoreLocalhost
            !sslSupport
            sslSocketFactory instanceof DummySSLSocketFactory
        }
    }

    void 'recorder configuration is overridden by map arguments'() {
        given:
        def recorder = new ProxyRecorder(
                tapeRoot: new File(tmpdir, 'tapes'),
                proxyPort: 1337,
                defaultMode: READ_ONLY,
                proxyTimeout: 30000,
                ignoreHosts: ['localhost'],
                ignoreLocalhost: true,
                sslSupport: true,
                sslSocketFactory: new SSLSocketFactory(null)
        )

        expect:
        with(recorder) {
            tapeRoot == new File(tmpdir, 'tapes')
            proxyPort == 1337
            defaultMode == READ_ONLY
            proxyTimeout == 30000
            ignoreHosts == ['localhost']
            ignoreLocalhost
            sslSupport
            sslSocketFactory instanceof SSLSocketFactory
        }
    }

    void 'recorder picks up configuration from properties'() {
        given:
        def properties = new Properties()
        properties.with {
            setProperty('betamax.tapeRoot', "${this.tmpdir}tapes".toString())
            setProperty('betamax.proxyPort', '1337')
            setProperty('betamax.defaultMode', 'READ_ONLY')
            setProperty('betamax.proxyTimeout', '30000')
            setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
            setProperty('betamax.ignoreLocalhost', 'true')
            setProperty('betamax.sslSupport', 'true')
        }

        and:
        def recorder = new ProxyRecorder(properties)

        expect:
        with(recorder) {
            tapeRoot == new File(tmpdir, 'tapes')
            proxyPort == 1337
            defaultMode == READ_ONLY
            proxyTimeout == 30000
            ignoreHosts == ['localhost', '127.0.0.1']
            ignoreLocalhost
            sslSupport
        }
    }

    void 'recorder picks up configuration from properties file'() {
        given:
        def propertiesFile = new File(tmpdir, 'betamax.properties')
        def properties = new Properties()
        properties.with {
            setProperty('betamax.tapeRoot', "${this.tmpdir}/tapes".toString())
            setProperty('betamax.proxyPort', '1337')
            setProperty('betamax.defaultMode', 'READ_ONLY')
            setProperty('betamax.proxyTimeout', '30000')
            setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
            setProperty('betamax.ignoreLocalhost', 'true')
            setProperty('betamax.sslSupport', 'true')
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        Recorder.classLoader.addURL(new File(tmpdir).toURL())

        and:
        def recorder = new ProxyRecorder()

        expect:
        with(recorder) {
            tapeRoot == new File(tmpdir, 'tapes')
            proxyPort == 1337
            defaultMode == READ_ONLY
            proxyTimeout == 30000
            ignoreHosts == ['localhost', '127.0.0.1']
            ignoreLocalhost
            sslSupport
        }

        cleanup:
        propertiesFile.delete()
    }

    void 'constructor arguments take precendence over a properties file'() {
        given:
        def propertiesFile = new File(tmpdir, 'betamax.properties')
        def properties = new Properties()
        properties.with {
            setProperty('betamax.tapeRoot', "${this.tmpdir}/tapes".toString())
            setProperty('betamax.proxyPort', '1337')
            setProperty('betamax.defaultMode', 'READ_ONLY')
            setProperty('betamax.proxyTimeout', '30000')
            setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
            setProperty('betamax.ignoreLocalhost', 'true')
            setProperty('betamax.sslSupport', 'true')
        }
        propertiesFile.withWriter { writer ->
            properties.store(writer, null)
        }

        and:
        Recorder.classLoader.addURL(new File(tmpdir).toURL())

        and:
        def recorder = new ProxyRecorder(
                tapeRoot: new File('test/fixtures/tapes'),
                proxyPort: 1234,
                defaultMode: WRITE_ONLY,
                proxyTimeout: 10000,
                ignoreHosts: ['github.com'],
                ignoreLocalhost: false,
                sslSupport: false
        )

        expect:
        with(recorder) {
            tapeRoot == new File('test/fixtures/tapes')
            proxyPort == 1234
            defaultMode == WRITE_ONLY
            proxyTimeout == 10000
            ignoreHosts == ['github.com']
            !ignoreLocalhost
            !sslSupport
        }

        cleanup:
        propertiesFile.delete()
    }
}
