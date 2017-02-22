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

package software.betamax.tape

import com.google.common.io.Files
import okhttp3.*
import software.betamax.tape.yaml.YamlTapeLoader
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_OK
import static software.betamax.TapeMode.READ_WRITE

class ContentTypeSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)
  @Shared Tape tape = loader.loadTape('tape_spec')
  @Shared File image = new File(Class.getResource("/image.png").toURI())

  @Shared Response successResponse = new Response.Builder()
      .code(HTTP_OK)
      .body(ResponseBody.create(MediaType.parse("text/plain"), "OK"))
      .build()

  void setup() {
    tape.mode = READ_WRITE
  }

  void 'can record post requests with an image content-type'() {
    given: 'a request with some content'
    def imagePostRequest = new Request.Builder()
        .method("POST", RequestBody.create(MediaType.parse("image/png"), image.bytes))
        .url("http://github.com/")
        .build()

    when: 'the request and its response are recorded'
    tape.record(imagePostRequest, successResponse)

    then: 'the request body is stored on the tape'
    def interaction = tape.interactions[-1]
    interaction.request.body == imagePostRequest.bodyAsBinary
  }
}
