package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.RequestBody
import okreplay.ComposedMatchRule
import okreplay.MatchRules
import okreplay.RecordedRequest
import okreplay.YamlTapeLoader
import spock.lang.*

import static com.google.common.net.HttpHeaders.ACCEPT
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.JSON_UTF_8
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8

@Issue("https://github.com/robfletcher/betamax/issues/9")
class RequestMatchingSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)

  @Unroll('#method request for #uri returns "#responseText"')
  void "default match is method and uri"() {
    given:
    def yaml = """\
!tape
name: method and url tape
interactions:
  - !!okreplay.RecordedInteraction [
    '2011-08-23T20:24:33.000Z',
    !!okreplay.RecordedRequest [
      'GET',
      'http://xkcd.com/'
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: text/plain},
      !!binary "R0VUIG1ldGhvZCByZXNwb25zZSBmcm9tIHhrY2QuY29t"
    ]
  ]
  - !!okreplay.RecordedInteraction [
    '2011-08-23T20:24:33.000Z',
    !!okreplay.RecordedRequest [
      'POST',
      'http://xkcd.com/'
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: text/plain},
      !!binary "UE9TVCBtZXRob2QgcmVzcG9uc2UgZnJvbSB4a2NkLmNvbQ=="
    ]
  ]
  - !!okreplay.RecordedInteraction [
    '2011-08-23T20:24:33.000Z',
    !!okreplay.RecordedRequest [
      'GET',
      'http://qwantz.com/'
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: text/plain},
      !!binary "R0VUIG1ldGhvZCByZXNwb25zZSBmcm9tIHF3YW50ei5jb20="
    ]
  ]
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))

    when:
    def response = tape.play(request)

    then:
    response.getBodyAsText() == responseText

    where:
    method | uri
    "GET"  | "http://xkcd.com/"
    "POST" | "http://xkcd.com/"
    "GET"  | "http://qwantz.com/"

    responseText = "$method method response from ${uri.toURI().host}"
    request = new RecordedRequest.Builder()
        .url(uri)
        .method(method, method == 'GET' ? null : RequestBody.create(MediaType.parse("text/plain"),
        "O HAI!"))
        .build()
  }

  void "can match based on host"() {
    given:
    def yaml = """\
!tape
name: host match tape
interactions:
  - !!okreplay.RecordedInteraction [
    '2011-08-23T20:24:33.000Z',
    !!okreplay.RecordedRequest [
      'GET',
      'http://xkcd.com/936/'
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: text/plain},
      !!binary "R0VUIG1ldGhvZCByZXNwb25zZSBmcm9tIHhrY2QuY29t"
    ]
  ]
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))
    tape.matchRule = MatchRules.host

    when:
    def request = new RecordedRequest.Builder()
        .url("http://xkcd.com/875")
        .build()
    def response = tape.play(request)

    then:
    response.getBodyAsText() == "GET method response from xkcd.com"
  }

  @Unroll('request with Accept: #acceptHeader returns "#responseText"')
  void "can match based on headers"() {
    given:
    def yaml = """\
!tape
name: headers match tape
interactions:
  - !!okreplay.RecordedInteraction [
    '2013-10-01T13:27:37.000Z',
    !!okreplay.RecordedRequest [
      'GET',
      'http://httpbin.org/get',
      {Accept: application/json}
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: application/json},
      !!binary "eyAibWVzc2FnZSI6ICJKU09OIGRhdGEiIH0="
    ]
  ]
  - !!okreplay.RecordedInteraction [
    '2013-10-01T13:34:33.000Z',
    !!okreplay.RecordedRequest [
      'GET',
      'http://httpbin.org/get',
      {Accept: text/plain}
    ],
    !!okreplay.RecordedResponse [
      200,
      {Content-Type: text/plain},
      !!binary "UGxhaW4gdGV4dCBkYXRh"
    ]
  ]
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))
    tape.matchRule = ComposedMatchRule.of(MatchRules.method, MatchRules.uri, MatchRules.accept)

    when:
    def request = new RecordedRequest.Builder()
        .addHeader(ACCEPT, acceptHeader)
        .url("http://httpbin.org/get")
        .build()
    def response = tape.play(request)

    then:
    response.header(CONTENT_TYPE) == acceptHeader
    response.getBodyAsText() == responseText

    where:
    acceptHeader                                    | responseText
    JSON_UTF_8.withoutParameters().toString()       | '{ "message": "JSON data" }'
    PLAIN_TEXT_UTF_8.withoutParameters().toString() | "Plain text data"
  }
}
