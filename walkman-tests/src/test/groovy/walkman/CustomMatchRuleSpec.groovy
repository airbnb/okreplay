package walkman

import com.google.common.base.Optional
import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.junit.ClassRule
import spock.lang.*

import static com.google.common.net.HttpHeaders.ACCEPT
import static walkman.MatchRules.method
import static walkman.MatchRules.uri

@Issue("https://github.com/robfletcher/betamax/issues/43")
@Unroll
class CustomMatchRuleSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = WalkmanConfig.builder().tapeRoot(tapeRoot).build()
  @Shared def interceptor = new WalkmanInterceptor()
  @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  @Shared def url = "http://freeside.co/betamax"
  @Shared def acceptHeaderRule = new MatchRule() {
    @Override
    boolean isMatch(Request a, Request b) {
      a.header(ACCEPT) == b.header(ACCEPT)
    }
  }

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void setupSpec() {
    new File(tapeRoot, "custom_match_rule_spec.yaml").withWriter { writer ->
      writer << """\
!tape
name: custom match rule spec
interactions:
  - !!walkman.RecordedInteraction [
    '2013-10-31T07:44:59.023Z',
    !!walkman.RecordedRequest [
      GET,
      '$url',
      {
        Accept: 'text/plain',
        X-Whatever: 'some random value'
      }
    ],
    !!walkman.RecordedResponse [
      200,
      {
        Content-Length: '12',
        Content-Type: 'text/plain',
        Date: 'Tue, 01 Oct 2013 21:53:58 GMT'
      },
      !!binary "SGVsbG8gV29ybGQh"
    ]
  ] 
"""
    }
  }

  def cleanup() {
    recorder.stop()
  }

  void "#verb request with Accept header #acceptHeader #description using #rules"() {
    given:
    recorder.start("custom match rule spec", Optional.absent(),
        Optional.of(ComposedMatchRule.of(rules)))

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
