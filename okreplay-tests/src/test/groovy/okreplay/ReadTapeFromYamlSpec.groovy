package okreplay

import com.google.common.io.Files
import org.yaml.snakeyaml.constructor.ConstructorException
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.common.net.HttpHeaders.*
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
class ReadTapeFromYamlSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)

  void "can load a valid tape with a single interaction"() {
    given:
    def yaml = """\
!tape
name: single_interaction_tape
interactions:
- recorded: 2011-08-23T22:41:40.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""
    when:
    def tape = loader.readFrom(new StringReader(yaml))
    def utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.set(2011, 7, 23, 22, 41, 40)
    utc.set(Calendar.MILLISECOND, 0)

    then:
    tape.name == "single_interaction_tape"
    tape.interactions.size() == 1
    tape.interactions[0].recorded == utc.time
    tape.interactions[0].request.method == "GET"
    tape.interactions[0].request.uri == "http://icanhascheezburger.com/".toURI()
    tape.interactions[0].response.status == HTTP_OK
    tape.interactions[0].response.header(CONTENT_TYPE) == "text/plain"
    tape.interactions[0].response.header(CONTENT_LANGUAGE) == "en-GB"
    tape.interactions[0].response.body() == "O HAI!"
  }

  void "can load a valid tape with multiple interactions"() {
    given:
    def yaml = """\
!tape
name: multiple_interaction_tape
interactions:
- recorded: 2011-08-23T23:41:40.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
- recorded: 2011-08-23T23:41:40.000Z
  request:
    method: GET
    uri: http://en.wikipedia.org/wiki/Hyper_Text_Coffee_Pot_Control_Protocol
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 418
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: I'm a teapot
"""
    when:
    def tape = loader.readFrom(new StringReader(yaml))

    then:
    tape.interactions.size() == 2
    tape.interactions[0].request.uri == "http://icanhascheezburger.com/".toURI()
    tape.interactions[1].request.uri == "http://en.wikipedia.org/wiki/Hyper_Text_Coffee_Pot_Control_Protocol".toURI()
    tape.interactions[0].response.status == HTTP_OK
    tape.interactions[1].response.status == 418
    tape.interactions[0].response.body() == "O HAI!"
    tape.interactions[1].response.body() == "I'm a teapot"
  }

  void "reads request headers"() {
    given:
    def yaml = """\
!tape
name: single_interaction_tape
interactions:
- recorded: 2011-08-23T22:41:40.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""
    when:
    def tape = loader.readFrom(new StringReader(yaml))

    then:
    tape.interactions[0].request.header(ACCEPT_LANGUAGE) == "en-GB,en"
    tape.interactions[0].request.header(IF_NONE_MATCH) == "b00b135"
  }

  void "barfs on non-yaml data"() {
    given:
    def yaml = "{}][: THIS IS NOT YAML"

    when:
    loader.readFrom(new StringReader(yaml))

    then:
    thrown TapeLoadException
  }

  void "barfs on yaml data which is not a YamlTape"() {
    given:
    def yaml = "this is valid yaml"

    when:
    loader.readFrom(new StringReader(yaml))

    then:
    thrown TapeLoadException
  }

  void "barfs on an invalid record date"() {
    given:
    def yaml = """\
!tape
name: invalid_date_tape
interactions:
- recorded: THIS IS NOT A DATE!
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""
    when:
    loader.readFrom(new StringReader(yaml))

    then:
    def e = thrown(TapeLoadException)
    e.cause instanceof ConstructorException
  }

}
