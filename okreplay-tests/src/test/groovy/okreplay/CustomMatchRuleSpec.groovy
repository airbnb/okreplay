package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.junit.ClassRule
import spock.lang.*

import static com.google.common.net.HttpHeaders.ACCEPT
import static okreplay.MatchRules.method
import static okreplay.MatchRules.uri

@Issue("https://github.com/robfletcher/betamax/issues/43")
@Unroll
class CustomMatchRuleSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = new OkReplayConfig.Builder()
      .tapeRoot(tapeRoot)
      .interceptor(new OkReplayInterceptor())
      .build()
  @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)
  @Shared def url = "http://freeside.co/betamax"
  @Shared def acceptHeaderRule = new MatchRule() {
    @Override
    boolean isMatch(Request a, Request b) {
      a.header(ACCEPT) == b.header(ACCEPT)
    }
  }

  def client = new OkHttpClient.Builder()
      .addInterceptor(configuration.interceptor())
      .build()

  void setupSpec() {
    new File(tapeRoot, "custom_match_rule_spec.yaml").withWriter { writer ->
      writer << """\
!tape
name: custom match rule spec
interactions:
- recorded: 2013-10-31T07:44:59.023Z
  request:
    method: GET
    uri: $url
    headers:
      Accept: text/plain
      X-Whatever: some random value
  response:
    status: 200
    headers:
      Content-Length: '12'
      Content-Type: text/plain
      Date: Tue, 01 Oct 2013 21:53:58 GMT
    body: Hello World!
"""
    }
  }

  def cleanup() {
    recorder.stop()
  }

  void "#verb request with Accept header #acceptHeader #description using #rules"() {
    given:
    recorder.start("custom match rule spec", null, ComposedMatchRule.of(rules))

    and:
    def body = RequestBody.create(MediaType.parse("text/plain"), "foo")
    def request = new okhttp3.Request.Builder()
        .url(url)
        .addHeader(ACCEPT, acceptHeader)
        .method(verb, verb == "GET" ? null : body)
        .build()

    expect:
    recorder.tape.seek(OkHttpRequestAdapter.adapt(request)) == shouldMatch

    where:
    verb   | acceptHeader       | rules                           | shouldMatch
    "GET"  | "text/plain"       | [method, uri]                   | true
    "POST" | "text/plain"       | [method, uri]                   | false
    "GET"  | "text/plain"       | [acceptHeaderRule]              | true
    "GET"  | "application/json" | [acceptHeaderRule]              | false
    "GET"  | "text/plain"       | [method, uri, acceptHeaderRule] | true
    "POST" | "text/plain"       | [method, uri, acceptHeaderRule] | false
    "GET"  | "application/json" | [method, uri, acceptHeaderRule] | false

    description = shouldMatch ? "matches" : "does not match"
  }
}
