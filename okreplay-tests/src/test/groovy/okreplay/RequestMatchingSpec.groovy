package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.RequestBody
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
name: method and uri tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: POST
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: POST method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://qwantz.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from qwantz.com
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))

    when:
    def response = tape.play(request)

    then:
    response.bodyAsText() == responseText

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
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/936/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
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
    response.bodyAsText() == "GET method response from xkcd.com"
  }

  @Unroll('request with Accept: #acceptHeader returns "#responseText"')
  void "can match based on headers"() {
    given:
    def yaml = """\
!tape
name: headers match tape
interactions:
- recorded: 2013-10-01T13:27:37.000Z
  request:
    method: GET
    uri: http://httpbin.org/get
    headers:
      Accept: application/json
  response:
    status: 200
    headers:
      Content-Type: application/json
    body: |-
      { "message": "JSON data" }
- recorded: 2013-10-01T13:34:33.000Z
  request:
    method: GET
    uri: http://httpbin.org/get
    headers:
      Accept: text/plain
  response:
    status: 200
    headers:
      Content-Type: text/plain
    body: Plain text data
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
    response.bodyAsText() == responseText

    where:
    acceptHeader                                    | responseText
    JSON_UTF_8.withoutParameters().toString()       | '{ "message": "JSON data" }'
    PLAIN_TEXT_UTF_8.withoutParameters().toString() | "Plain text data"
  }
}
