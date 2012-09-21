package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTapeLoader
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class YamlTapeLoaderSpec extends Specification {

    @Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')

    void setupSpec() {
        tapeRoot.mkdirs()
    }

    @Issue('https://github.com/robfletcher/betamax/issues/12')
    void 'tape is not re-written if the content has not changed'() {
        given:
        def tapeName = 'yaml tape loader spec'
        def loader = new YamlTapeLoader(tapeRoot)
        def tapeFile = loader.fileFor(tapeName)

        tapeFile.text = """\
!tape
name: $tapeName
interactions:
- recorded: 2011-08-23T22:41:40.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: 'en-GB,en', If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""

        and:
        def tape = loader.loadTape(tapeName)

        when:
        loader.writeTape(tape)

        then:
        tapeFile.text == old(tapeFile.text)
    }
}
