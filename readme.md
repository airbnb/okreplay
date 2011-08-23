# Betamax

A Groovy testing tool inspired by Ruby's [VCR][1]. Betamax can record and play back HTTP interactions made by your app
so that your tests can run without any real HTTP traffic going to external URLs. The first time a test is run any HTTP
traffic is recorded to a _tape_ and subsequent runs will play back the HTTP response without really connecting to the
external endpoint.

Tapes are stored to disk as JSON and can be modified (or even created) by hand. Different tests can use different tapes
to simulate varying responses from external endpoints.

## Usage

To use Betamax you just need to annotate your JUnit test or [Spock][2] specifications with `@Betamax(tape="tape_name")`
and include a `betamax.Recorder` Rule.

### JUnit example

    import betamax.Betamax
    import betamax.Recorder
    import org.junit.*

    class MyTest {

	        @Rule public Recorder recorder = Recorder.instance

            @Betamax(tape="my_tape")
            @Test
            void testMethodThatAccessesExternalWebService() {

            }

    }

### Spock example

    import betamax.Betamax
    import betamax.Recorder
    import org.junit.*
    import spock.lang.*

    class MySpec extends Specification {

	        @Rule Recorder recorder = Recorder.instance

            @Betamax(tape="my_tape")
            def "test method that accesses external web service"() {

            }

    }
    
## Configuration

Betamax stores tapes in `src/test/resources/betamax/tapes`. You can change this by assigning a `File` object to `Recorder.instance.tapeRoot`. Likewise you can override the default port (`5555`) by setting `Recorder.instance.port`.

## Caveats

By default [Apache _HTTPClient_][3] takes no notice of Java's HTTP proxy settings. If you are using HTTPClient then the
Betamax proxy can only intercept HTTP traffic if the client is set up to use a [`ProxySelectorRoutePlanner`][5].

### Configuring HTTPClient

    def client = new DefaultHttpClient()
    client.routePlanner = new ProxySelectorRoutePlanner(client.connectionManager.schemeRegistry, ProxySelector.default)

The same is true of [Groovy _HTTPBuilder_][4] and its [_RESTClient_][6] variant as they are wrappers around
_HTTPClient_.

### Configuring HTTPBuilder

    def http = new HTTPBuilder("http://groovy.codehaus.org")
    def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
    http.client.routePlanner = routePlanner

_HTTPBuilder_ also includes a [_HttpURLClient_][7] class which needs no special configuration as it uses a
`java.net.URLConnection` rather than _HTTPClient_.

## Roadmap

* Configure Recorder via annotation
* Test with HTTPClient 3.x
* Description in interactions
* Non-text responses
* Multipart requests
* Rotate multiple responses for same URL on same tape
* Throw exceptions if tape not inserted & proxy gets hit
* Allow groovy evaluation in tape files
* Match requests based on URI, host, path, method, body, headers
* Optionally re-record tapes after TTL expires
* Record modes

[1]:https://github.com/myronmarston/vcr
[2]:http://spockframework.org/
[3]:http://hc.apache.org/httpcomponents-client-ga/httpclient/index.html
[4]:http://groovy.codehaus.org/modules/http-builder/
[5]:http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/ProxySelectorRoutePlanner.html
[6]:http://groovy.codehaus.org/modules/http-builder/doc/rest.html
[7]:http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html
