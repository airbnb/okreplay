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

package software.betamax.proxy

import com.google.common.io.Files
import java.net.Authenticator.RequestorType
import org.junit.ClassRule
import software.betamax.ProxyConfiguration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.util.server.EchoHandler
import software.betamax.util.server.SimpleServer
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import static software.betamax.TapeMode.READ_ONLY

@Issue("https://github.com/betamaxteam/betamax/issues/174")
@Betamax(mode = READ_ONLY)
class ProxyAuthenticationSpec extends Specification {
    static final String PROXY_USERNAME = "dummy"
    static final String PROXY_PASSWORD = "password"

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().
            proxyAuth(PROXY_USERNAME, PROXY_PASSWORD).
            tapeRoot(tapeRoot).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @AutoCleanup("stop") def endpoint = new SimpleServer(EchoHandler)

    String oldProxyUser
    String oldProxyPassword
    Authenticator oldAuthenticator

    void setup() {
        oldProxyUser = System.getProperty("http.proxyUser")
        oldProxyPassword = System.getProperty("http.proxyPassword")
        Authenticator.setDefault(new ProxyAuthenticator())
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
    }

    void cleanup() {
        if (oldProxyUser != null) System.setProperty("http.proxyUser", oldProxyUser)
        else System.clearProperty("http.proxyUser")

        if (oldProxyPassword != null) System.setProperty("http.proxyPassword", oldProxyPassword)
        else System.clearProperty("http.proxyPassword")

        Authenticator.setDefault(null)
    }

    void "proxy responds with 407 if proxy credentials are not provided"() {
        given:
        endpoint.start()

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()
        def status = connection.responseCode

        then:
        status == 407
    }

    void "proxy responds with 407 if proxy credentials are incorrect"() {
        given:
        endpoint.start()

        and: "Proxy invalid authentication settings"
        System.setProperty("http.proxyUser", PROXY_USERNAME)
        System.setProperty("http.proxyPassword", "kdsfjgnask")
        System.setProperty("http.maxRedirects", "3")

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()
        def status = connection.responseCode

        then:
        status == 407
    }

    void "proxy forwards the request if proxy credentials are correct"() {
        given:
        endpoint.start()

        and: "Proxy valid authentication settings"
        System.setProperty("http.proxyUser", PROXY_USERNAME)
        System.setProperty("http.proxyPassword", PROXY_PASSWORD)

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()
        def status = connection.responseCode

        then:
        status == 200
    }

    private static class ProxyAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            if (requestorType == RequestorType.PROXY && System.getProperty("http.proxyUser") != null) {
                return new PasswordAuthentication(
                        System.getProperty("http.proxyUser"),
                        System.getProperty("http.proxyPassword")?.toCharArray() ?: new char[0])
            }

            return null
        }
    }

}
