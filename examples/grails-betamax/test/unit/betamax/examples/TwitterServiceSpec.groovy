package betamax.examples

import java.util.logging.*
import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.*
import grails.test.mixin.TestFor
import grails.util.BuildSettingsHolder
import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.junit.Rule
import spock.lang.Specification
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

@TestFor(TwitterService)
class TwitterServiceSpec extends Specification {

    File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File('examples/grails-betamax')
    def configuration = ProxyConfiguration.builder().tapeRoot(new File(baseDir, 'test/resources/tapes')).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    TwitterService service = new TwitterService()

    void setupSpec() {
        def log = Logger.getLogger('betamax')
        log.addHandler(new ConsoleHandler())
    }

    void setup() {
        def restClient = new RESTClient()
        restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(configuration.proxyHost, configuration.proxyPort, "http"))
        service.restClient = restClient
    }

    @Betamax(tape = 'twitter success')
    void 'returns aggregated twitter client stats when a successful response is received'() {
        when:
        def clients = service.tweetsByClient('#gr8conf')

        then:
        clients.size() == 6
        clients['Twitterrific'] == 1
        clients['Echofon'] == 1
        clients['Tweetbot for Mac'] == 1
        clients['Twitter for Mac'] == 5
        clients['Twitter for iPhone'] == 1
        clients['Twitter for Android'] == 1
    }

    @Betamax(tape = 'twitter success')
    void 'only retrieves tweets containing the search term'() {
        when:
        def tweets = service.tweets('#gr8conf')

        then:
        tweets.size() == 10
        tweets.every { it.text=~/(?i)#gr8conf/ }
    }

    @Betamax(tape = 'twitter rate limit')
    void 'sets an error status when twitter rate limit is exceeded'() {
        when:
        service.tweetsByClient('#gr8conf')

        then:
        thrown TwitterException
    }
}
