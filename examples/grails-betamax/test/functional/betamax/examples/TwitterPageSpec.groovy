package betamax.examples

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.*
import com.gargoylesoftware.htmlunit.ProxyConfig
import geb.spock.GebSpec
import grails.util.BuildSettingsHolder
import org.apache.http.HttpHost
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.junit.Rule
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import spock.lang.Shared
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

class TwitterPageSpec extends GebSpec {

    File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File('examples/grails-betamax')
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(new File(baseDir, 'test/resources/tapes')).ignoreLocalhost(true)
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    void setupSpec() {
        def restClient = ApplicationHolder.application.mainContext.restClient
        restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(configuration.proxyHost, configuration.proxyPort, "http"))
    }

    void setup() {
        browser.baseUrl = 'http://localhost:8080/grails-betamax/'
        HtmlUnitDriver driver = browser.driver
        def proxyConfig = new ProxyConfig('localhost', 5555)
        proxyConfig.addHostsToProxyBypass('localhost')
        driver.webClient.proxyConfig = proxyConfig
        //driver.setJavascriptEnabled(true) //not work. see http://groups.google.com/group/webdriver/browse_thread/thread/6bbb18dca79c4e92?pli=1
    }

    @Betamax(tape = 'twitter success')
    void 'displays list of tweets based on query'() {
        given:
        go 'twitter'

        expect:
        title == 'Twitter Search Results'

        and:
        $('#tweets li').size() == 10
        $('#tweets li p')*.text().every { it=~/(?i)#gr8conf/ }
        $('#tweets li').eq(0).find('p').text() == 'Slides for my #gr8conf talk: \u2018Spock Soup to Nuts\u2019. Thanks to everyone who showed up! http://t.co/CNA9ertp'
    }

    @Betamax(tape = 'twitter success')
    void 'can follow a link to a twitter user'() {
        given:
        go 'twitter'

        when:
        $('#tweets li').eq(0).find('small a').click()

        then:
        $('.user-info .username').text() == '@zanthrash'
    }

}
