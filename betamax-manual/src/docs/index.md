---
title: Home
layout: index
version: 1.1.2
dev-version: 1.2-SNAPSHOT
---

## Introduction

Betamax is a tool for mocking external HTTP resources such as [web services][webservices] and [REST][rest] APIs in your tests. The project was inspired by the [VCR][vcr] library for Ruby.

You don't want 3rd party downtime, network issues or resource constraints (such as the Twitter API's [rate limit][twitterratelimit]) to break your tests. Writing custom _stub_ web server code and configuring the application to connect to a different URI when under test is tedious and might not accurately simulate the real service.

Betamax aims to solve these problems by intercepting HTTP connections initiated by your application and replaying previously _recorded_ responses.

The first time a test annotated with `@Betamax` is run any HTTP traffic is recorded to a _tape_ and subsequent test runs will play back the recorded HTTP response from the tape without actually connecting to the external server.

Betamax works with [JUnit][junit] and [Spock][spock]. Although it is written in [Groovy][groovy] Betamax can be used to test applications written in any JVM language.

Tapes are stored to disk as [YAML][yaml] files and can be modified (or even created) by hand and committed to your project's source control repository so they can be shared by other members of your team and used by your CI server. Different tests can use different tapes to simulate various response conditions. Each tape can hold multiple request/response interactions. An example tape file can be found [here][tapeexample].

## Versions

The current stable version of Betamax is _{{ page.version }}_.

The current development version of Betamax is _{{page.dev-version}}_.

## Implementations

Betamax comes in two flavors. The first is an HTTP and HTTPS proxy that can intercept traffic made in any way that respects Java's `http.proxyHost` and `http.proxyPort` system properties. The second is a simple wrapper for Apache _HttpClient_.

### The Betamax proxy

The proxy implementation can be used with HTTP traffic initiated from _java.net.URLConnection_, Apache _HttpClient_, etc. It runs an actual HTTP(S) proxy on Jetty and overrides the JVM proxy settings so that traffic is redirected via the proxy.

### The Betamax HttpClient wrapper

The _HttpClient_ wrapper is a simpler and faster implementation than the Betamax proxy but only works with _HttpClient_ (or things built on top of it such as the Groovy _Http Builder_). It is a good choice when you use _HttpClient_ instances that are injected into your classes. In your tests you simply inject an instance of _BetamaxHttpClient_ instead.

## Installation

Stable versions of Betamax are available from the Maven central repository. Stable and development versions are available from the [Sonatype OSS Maven repository][sonatype]. To install with your favourite build system see below.

Please note the Maven group changed between versions 1.0 and 1.1. Make sure you are specifying the group `co.freeside` when referencing Betamax in your build.

If you are installing a development version you will need to add the repository `http://oss.sonatype.org/content/groups/public/` to your build.

### Gradle

To use the Betamax proxy in a project using [Gradle][gradle] add the following dependency to your `build.gradle` file:

    testCompile 'co.freeside:betamax-proxy:{{ page.version }}'

Or to use the _HttpClient_ wrapper add this:

    testCompile 'co.freeside:betamax-httpclient:{{ page.version }}'

### Grails

To use the Betamax proxy in a [Grails][grails] app add the following to the `dependencies` block in your `grails-app/conf/BuildConfig.groovy` file:

    test 'co.freeside:betamax-proxy:{{ page.version }}'

Or to use the _HttpClient_ wrapper add this:

    test 'co.freeside:betamax-httpclient:{{ page.version }}'

### Maven

To use the Betamax proxy in a project using [Maven][maven] add the following dependency to your `pom.xml` file:

    <dependency>
      <scope>test</scope>
      <groupId>co.freeside</groupId>
      <artifactId>betamax-proxy</artifactId>
      <version>{{ page.version }}</version>
    </dependency>

Or to use the _HttpClient_ wrapper add this:

    <dependency>
      <scope>test</scope>
      <groupId>co.freeside</groupId>
      <artifactId>betamax-httpclient</artifactId>
      <version>{{ page.version }}</version>
    </dependency>

## Usage

To use Betamax you just need to annotate your JUnit test or Spock specifications with `@Betamax(tape="tape_name")` and include a `Recorder` Rule.

If you are using the Betamax proxy you need to use `ProxyRecorder` rule.

### JUnit

    import co.freeside.betamax.junit.Betamax;
    import co.freeside.betamax.junit.RecorderRule;
    import co.freeside.betamax.Recorder;
    import org.junit.*;

    public class MyTest {

        @Rule public RecorderRule recorder = new RecorderRule(new Recorder());

        @Betamax(tape="my tape")
        @Test
        public void testMethodThatAccessesExternalWebService() {

        }
    }

### Spock

    import co.freeside.betamax.junit.Betamax
    import co.freeside.betamax.junit.RecorderRule
    import co.freeside.betamax.Recorder
    import org.junit.*
    import spock.lang.*

    class MySpec extends Specification {

        @Rule RecorderRule recorder = new RecorderRule(new Recorder())

        @Betamax(tape='my tape')
        void 'feature that accesses external web service'() {

        }
    }

### Recording and playback

Betamax will record to the current tape when it intercepts any HTTP request that does not match anything that is already on the tape. If a matching recorded interaction _is_ found then the proxy does not forward the request to the target URI but instead returns the previously recorded response to the client.

### Matching requests

By default recorded interactions are matched based on the _method_ and _URI_ of the request. For most scenarios this is adequate. However, you can modify the matching behaviour by specifying a _match_ argument on the `@Betamax` annotation. Any combination of instances of the `co.freeside.betamax.MatchRule` enum can be used. If multiple rules are used then only a recorded interaction that matches all of them will be played back. `MatchRule` options are:

`method`
: the request method, _GET_, _POST_, etc.

`uri`
: the full URI of the request target. This includes any query string.

`body`
: the request body. This can be useful for testing connections to RESTful services that accept _POST_ data.

`host`
: the host of the target URI. For example the host of `http://search.twitter.com/search.json` is `search.twitter.com`.

`path`
: the path of the target URI. For example the path of `http://search.twitter.com/search.json` is `/search.json`.

`port`
: the port of the target URI.

`query`
: the query string of the target URI.

`fragment`
: the fragment of the target URI. i.e. anything after a `#`.

`headers`
: the request headers. If this rule is used then _all_ headers on the intercepted request must match those on the previously recorded request.

Note that request matching is **not** done at all when using `READ_SEQUENTIAL` or `WRITE_SEQUENTIAL` tape modes.

### Tape modes

Betamax supports different read/write modes for tapes. The tape mode is set by adding a `mode` argument to the `@Betamax` annotation.

`READ_WRITE`
: This is the default mode. If the proxy intercepts a request that matches a recording on the tape then the recorded response is played back. Otherwise the request is forwarded to the target URI and the response recorded.

`READ_ONLY`
: The proxy will play back responses from tape but if it intercepts an unknown request it will not forward it to the target URI or record anything, instead it responds with a `403: Forbidden` status code.

`WRITE_ONLY`
: The proxy will always forward the request to the target URI and record the response regardless of whether or not a matching request is already on the tape. Any existing recorded interactions will be overwritten.

`READ_SEQUENTIAL`
: The proxy will replay recordings from the tape in strict sequential order. If the current request does not match the next recorded request on the tape an error is raised. Likewise if a request arrives after all the recordings have already been played back an error is raised. This is primarily useful for testing stateful endpoints. Note that in this mode multiple recordings that match the current request may exist on the tape.

`WRITE_SEQUENTIAL`
: The proxy will behave as per `WRITE_ONLY` except that no matching on existing requests is done. All requests are recorded in sequence regardless of whether they match an existing recording or not. This mode is intended for preparing tapes for use with `READ_SEQUENTIAL` mode.

### Ignoring certain hosts

Sometimes you may need to have Betamax ignore traffic to certain hosts. A typical example would be if you are using the Betamax proxy when end-to-end testing a web application using something like _[HtmlUnit][htmlunit]_ - you would not want Betamax to intercept connections to _localhost_ as that would mean traffic between _HtmlUnit_ and your app was recorded and played back!

In such a case you can simply configure the `ignoreHosts` property of the `co.freeside.betamax.Recorder` object. The property accepts a list of hostnames or IP addresses. These can include wildcards at the start or end, for example `"*.mydomain.com"`.

If you need to ignore connections to _localhost_ you can simply set the `ignoreLocalhost` property to `true`.

### Editing tape files

Tape files are stored as _YAML_ so that they should be reasonably easy to edit by hand. HTTP request and response bodies are stored as text for most common textual MIME types. Binary data for things like images is also stored but is not practical to edit by hand. In some cases where the text contains non-printable characters then text data will be stored as binary.

## Proxy compatibility

If you're using the Betamax proxy there are some compatibility issues you should be aware of:

### Java 6

Under Java 6 it is not possible to proxy connections to URLs whose host is `localhost` or `127.0.0.1`. The workaround is to use the hostname or public IP address of the machine instead. This is a [known issue](http://bugs.sun.com/view_bug.do?bug_id=6737819) that is fixed in Java 7.

### Apache HttpClient

The default implementations of [Apache _HttpClient_][httpclient] takes no notice of Java's HTTP proxy settings. The Betamax proxy can only intercept traffic from HttpClient if the client instance is set up to use a [`ProxySelectorRoutePlanner`][proxyselector]. When Betamax is not active this will mean HttpClient traffic will be routed via the default proxy configured in Java (if any).

In a dependency injection context such as a [Grails][grails] app you can just inject a proxy-configured _HttpClient_ instance into your class-under-test.

The _HttpClient_ library provides an implementation called [SystemDefaultHttpClient](http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/SystemDefaultHttpClient.html) that _does_ use the JVM proxy settings. Ideally you can use that. In addition, Betamax provides a convenient `HttpRoutePlanner` implementation that you can use to configure instances of other _HttpClient_ types. For example:

    DefaultHttpClient client = new DefaultHttpClient();
    BetamaxRoutePlanner.configure(client);

### Groovy HTTPBuilder

[Groovy _HTTPBuilder_][httpbuilder] and its [_RESTClient_][restclient] variant are wrappers around _HttpClient_ so the same proxy configuration needs to be applied. For example:

    def http = new HTTPBuilder('http://groovy.codehaus.org')
    BetamaxRoutePlanner.configure(http.client)

_HTTPBuilder_ also includes a [_HttpURLClient_][httpurlclient] class which needs no special configuration as it uses a `java.net.URLConnection` rather than _HttpClient_.

### Apache HttpClient 3.x

[_HttpClient_ 3.x][httpclient3] is no longer supported but still fairly widely used. It does not take any notice of Java's HTTP proxy settings and does not have the `HttpRoutePlanner` facility that _HttpClient_ 4.x does. This means Betamax cannot work as seamlessly. You must set the host and port of the Betamax proxy on the _HttpClient_ instance explicitly and Betamax's `ignoreHosts` and `ignoreLocalhost` configuration properties will be completely ignored. For example:

    HttpClient client = new HttpClient();
    ProxyHost proxy = new ProxyHost("localhost", recorder.getProxyPort());
    client.getHostConfiguration().setProxyHost(proxy);

### WSLite

[_WSLite_][wslite] does not use the default JVM proxy settings at all. You will need to configure it to use the Betamax proxy. There is a `getProxy()` convenience method on `Recorder` that makes this very easy:

    def http = new RESTClient('http://freeside.co/betamax')
    def response = http.get(path: '/', proxy: recorder.proxy)

## HTTPS

As of version 1.1 the Betamax proxy can handle HTTPS traffic as well as HTTP. Because Betamax needs to be able to read the content of the request and response it is not actually a valid secure proxy. Betamax will only work if the certificate chain is broken.

To enable HTTP support you simply need to set the `sslEnabled` boolean property on the `Recorder` instance in your test or via Betamax configuration.

Note, this is not necessary if you are using the _BetamaxHttpClient_ wrapper class instead of the proxy. HTTPS is handled no differently to HTTP in that case.

### HTTPS with Apache HttpClient

Apache _HttpClient_ needs to be configured to use Betamax's HTTPS support:

    BetamaxHttpsSupport.configure(client);

## Configuration

The `Recorder` class has some configuration properties that you can override:

`tapeRoot`
: the base directory where tape files are stored. Defaults to `src/test/resources/betamax/tapes`.

`defaultMode`
: the default _TapeMode_ applied to an inserted tape when the _mode_ argument is not present on the `@Betamax` annotation.

`ignoreHosts`
: a list of hosts that will be ignored by the Betamax proxy. Any requests made to these hosts will proceed normally.

`ignoreLocalhost`
: if set to `true` the Betamax proxy will ignore connections to local addresses. This is equivalent to setting `ignoreHosts` to `["localhost", "127.0.0.1", InetAddress.localHost.hostName, InetAddress.localHost.hostAddress]`.

When using the Betamax proxy these additional properties are available:

`proxyPort`
: the port the Betamax proxy listens on. Defaults to `5555`.

`proxyTimeout`
: the number of milliseconds before the proxy will give up on a connection to the target server. A value of zero means the proxy will wait indefinitely. Defaults to `5000`.

`sslEnabled`
: if set to `true` the Betamax proxy will also intercept HTTPS traffic.

`sslSocketFactory`
: the instance of `org.apache.http.conn.ssl.SSLSocketFactory` that the Betamax proxy will use to connect to the target when using HTTPS. By default Betamax will use its own socket factory with a self-signed certificate but if you are connecting to a server that has more stringent security requirements or needs SSL authentication you can override this. 

If you have a file called `BetamaxConfig.groovy` or `betamax.properties` somewhere in your classpath it will be picked up by the `Recorder` class.

### Example _BetamaxConfig.groovy_ script

    betamax {
        tapeRoot = new File('src/test/resources/betamax/tapes')
        useProxy = true
        proxyPort = 5555
        proxyTimeout = 5000
        defaultMode = TapeMode.READ_WRITE
        ignoreHosts = ['localhost', '127.0.0.1']
        ignoreLocalhost = false
        sslEnabled = false
        sslSocketFactory = new SSLSocketFactory(myTrustStore)
    }

### Example _betamax.properties_ file

    betamax.tapeRoot=src/test/resources/betamax/tapes
    betamax.useProxy=true
    betamax.proxyPort=5555
    betamax.proxyTimeout=5000
    betamax.defaultMode=READ_WRITE
    betamax.ignoreHosts=localhost,127.0.0.1
    betamax.ignoreLocalhost=false
    betamax.sslEnabled=false

## Caveats

### Security

The Betamax proxy is a testing tool and not a spec-compliant HTTP proxy. It ignores _any_ and _all_ headers that would normally be used to prevent a proxy caching or storing HTTP traffic. You should ensure that sensitive information such as authentication credentials is removed from recorded tapes before committing them to your app's source control repository.

## Examples

Betamax's GitHub repository includes [an example Grails application][grailsexample].

## About

### Why "Betamax"?

Betamax is a JVM port of the [VCR][vcr] library for Ruby. It is named after _[Betamax][betamax]_, an obsolete format of [Video Cassette Recorder](http://en.wikipedia.org/wiki/Vcr).

### License

[Apache Software Licence, Version 2.0][licence]

### Issues

Please raise issues on Betamax's [GitHub issue tracker][issues]. Forks and pull requests are more than welcome.

### Dependencies

Betamax depends on the following libraries (you will need them available on your test classpath in order to use Betamax):

* [Groovy 1.8+][groovy]
* [Apache HttpClient][httpclient]
* [SnakeYAML][snakeyaml]
* [JUnit 4][junit]

In addition the Betamax proxy requires:

* [Jetty 7][jetty]

If your project gets dependencies from a [Maven][maven] repository these dependencies will be automatically included for you.

### Author

* [Rob Fletcher][github]

### Contributors

* [Marcin Erdmann](https://github.com/erdi)
* [Lari Hotari](https://github.com/lhotari)
* [Steve Ims](https://github.com/steveims)
* [Nobuhiro Sue](https://github.com/nobusue)

### Acknowledgements

Betamax is inspired by the [VCR][vcr] library for Ruby written by Myron Marston. Porting VCR to Groovy was suggested to me by [Jim Newbery][jim].

HTTPS proxy support was largely the work of [Lari Hotari][lari].

The documentation is built with [Jekyll][jekyll], [Twitter Bootstrap](http://twitter.github.com/bootstrap), [LESS][less], [Modernizr][modernizr], [jQuery][jquery] & [Google Code Prettify][prettify]. The fonts are _Kameron_, _Bitter_ and _Source Code Pro_.

[adhockery]:http://blog.freeside.co/ (Ad-Hockery)
[betamax]:http://en.wikipedia.org/wiki/Betamax
[git]:http://git-scm.com
[github]:http://github.com/robfletcher (Rob Fletcher on GitHub)
[gradle]:http://www.gradle.org/
[grails]:http://grails.org/
[grailsexample]:https://github.com/robfletcher/betamax/tree/master/examples/grails-betamax
[groovy]:http://groovy.codehaus.org/
[htmlunit]:http://htmlunit.sourceforge.net/
[httpbuilder]:http://groovy.codehaus.org/modules/http-builder/
[httpclient]:http://hc.apache.org/httpcomponents-client-ga/httpclient/index.html
[httpclient3]:http://hc.apache.org/httpclient-3.x/
[httpurlclient]:http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html
[issues]:https://github.com/robfletcher/betamax/issues
[jekyll]:http://jekyllrb.com/
[jetty]:http://www.eclipse.org/jetty/
[jquery]:http://jquery.com/
[jim]:http://tinnedfruit.com/
[junit]:http://www.junit.org/
[lari]:https://twitter.com/lhotari
[less]:http://lesscss.org/
[licence]:http://www.apache.org/licenses/LICENSE-2.0.html
[maven]:http://maven.apache.org/
[modernizr]:http://www.modernizr.com/
[prettify]:http://code.google.com/p/google-code-prettify/
[proxyselector]:http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/ProxySelectorRoutePlanner.html
[rest]:http://en.wikipedia.org/wiki/Representational_state_transfer
[restclient]:http://groovy.codehaus.org/modules/http-builder/doc/rest.html
[skeleton]:http://www.getskeleton.com/
[snakeyaml]:http://www.snakeyaml.org/
[sonatype]:https://oss.sonatype.org/content/repositories/snapshots/
[spock]:http://spockframework.org/
[tapeexample]:https://github.com/robfletcher/betamax/blob/master/src/test/resources/betamax/tapes/smoke_spec.yaml
[twitter]:http://twitter.com/rfletcherEW (@rfletcherEW on Twitter)
[twitterratelimit]:https://dev.twitter.com/docs/rate-limiting
[webservices]:http://en.wikipedia.org/wiki/Web_service
[vcr]:http://relishapp.com/myronmarston/vcr
[wslite]:https://github.com/jwagenleitner/groovy-wslite
[yaml]:http://yaml.org/
