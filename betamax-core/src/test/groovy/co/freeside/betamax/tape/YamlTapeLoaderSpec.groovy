/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTapeLoader
import com.google.common.io.Files
import spock.lang.*

class YamlTapeLoaderSpec extends Specification {

    @Shared @AutoCleanup('deleteDir') File tapeRoot = Files.createTempDir()

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
