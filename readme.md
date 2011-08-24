# Betamax

A Groovy testing tool inspired by Ruby's [VCR][1]. Betamax can record and play back HTTP interactions made by your app
so that your tests can run without any real HTTP traffic going to external URLs. The first time a test is run any HTTP
traffic is recorded to a _tape_ and subsequent runs will play back the HTTP response without really connecting to the
external endpoint.

Tapes are stored to disk as [YAML][8] files and can be modified (or even created) by hand and committed to your project's
source control repository so that other members of the team can use them when running tests. Different tests can use
different tapes to simulate varying responses from external endpoints. Each tape can hold multiple request/response
interactions but each must (currently) have a unique request method and URI.

An example tape file can be found [here][10].

## Project status

Betamax is at quite an early stage of development. It is usable and I would encourage users to give feedback, raise
issues etc. via GitHub.

Please bear in mind that the format and structure of tape files is subject to change at least until there is a first
stable release.

Betamax is not yet hosted on a maven repository. You can build from source or use the jar from the [downloads][9]
section. Dependency details can be found below.

## Dependencies

Betamax depends on the following libraries (you will need them available on your test classpath in order to use
Betamax):

* [Groovy](http://groovy.codehaus.org)
* [Apache HttpClient](http://hc.apache.org/httpcomponents-client-ga/)
* [Apache HttpCore NIO Extensions](http://hc.apache.org/httpcomponents-core-ga/httpcore-nio/index.html)
* [SnakeYAML](http://www.snakeyaml.org/)
* [JUnit 4](http://www.junit.org/)
* [Apache log4j](http://logging.apache.org/log4j/1.2/)

To see exact dependency versions used by Betamax see the `build.gradle` file.

## Usage

To use Betamax you just need to annotate your JUnit test or [Spock][2] specifications with `@Betamax(tape="tape_name")`
and include a `betamax.Recorder` Rule.

### JUnit example

```groovy
import betamax.Betamax
import betamax.Recorder
import org.junit.*

class MyTest {

	@Rule public Recorder recorder = new Recorder()

	@Betamax(tape="my_tape")
	@Test
	void testMethodThatAccessesExternalWebService() {

	}

}
```

### Spock example

```groovy
import betamax.Betamax
import betamax.Recorder
import org.junit.*
import spock.lang.*

class MySpec extends Specification {

	@Rule Recorder recorder = new Recorder()

	@Betamax(tape="my_tape")
	def "test method that accesses external web service"() {

	}

}
```

## Recording and playback

Betamax will record to the current tape when it intercepts any HTTP request with a combination of method and URI that
does not match anything that is already on the tape. If a recorded interaction with the same method and URI _is_ found
then the proxy does not forward the request to the target URI but instead returns the previously recorded response to
the requestor.

In future it will be possible to match recorded interactions based on criteria other than just method and URI.

## Security

Betamax is a testing tool and not a spec-compliant HTTP proxy. It ignores _any_ and _all_ headers that would normally be
used to prevent a proxy caching or storing HTTP traffic. You should ensure that sensitive information such as
authentication credentials is removed from recorded tapes before committing them to your app's source control
repository.

## Configuration

The `Recorder` class has some configuration properties that you can override:

* *tapeRoot*: the base directory where tape files are stored. Defaults to `src/test/resources/betamax/tapes`.
* *proxyPort*: the port the Betamax proxy listens on. Defaults to `5555`.

If you have a file called `BetamaxConfig.groovy` or `betamax.properties` somewhere in your classpath it will be picked
up by the `Recorder` constructor.

### Example _BetamaxConfig.groovy_ script

```groovy
betamax {
	tapeRoot = new File("test/fixtures/tapes")
	proxyPort = 1337
}
```

### Example _betamax.properties_ file

```properties
betamax.tapeRoot=test/fixtures/tapes
betamax.proxyPort=1337
```

## Caveats

By default [Apache _HttpClient_][3] takes no notice of Java's HTTP proxy settings. The Betamax proxy can only intercept
traffic from HttpClient if the client instance is set up to use a [`ProxySelectorRoutePlanner`][5]. When Betamax is not
active this will mean HttpClient traffic will be routed via the default proxy configured in Java (if any).

### Configuring HttpClient

```groovy
def client = new DefaultHttpClient()
client.routePlanner = new ProxySelectorRoutePlanner(client.connectionManager.schemeRegistry, ProxySelector.default)
```

The same is true of [Groovy _HTTPBuilder_][4] and its [_RESTClient_][6] variant as they are wrappers around
_HttpClient_.

### Configuring HTTPBuilder

```groovy
def http = new HTTPBuilder("http://groovy.codehaus.org")
def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
http.client.routePlanner = routePlanner
```

_HTTPBuilder_ also includes a [_HttpURLClient_][7] class which needs no special configuration as it uses a
`java.net.URLConnection` rather than _HttpClient_.

[1]:https://github.com/myronmarston/vcr
[2]:http://spockframework.org/
[3]:http://hc.apache.org/httpcomponents-client-ga/httpclient/index.html
[4]:http://groovy.codehaus.org/modules/http-builder/
[5]:http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/ProxySelectorRoutePlanner.html
[6]:http://groovy.codehaus.org/modules/http-builder/doc/rest.html
[7]:http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html
[8]:http://yaml.org/
[9]:https://github.com/robfletcher/betamax/archives/master
[10]:https://github.com/robfletcher/betamax/blob/master/src/test/resources/betamax/tapes/smoke_spec.yaml
