/*
 * Copyright 2012 the original author or authors.
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

package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.ResponseBody
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static java.util.concurrent.TimeUnit.SECONDS
import static okreplay.TapeMode.*

@Issue("https://github.com/robfletcher/betamax/issues/57")
class MultiThreadedTapeAccessSpec extends Specification {
  // TODO: only need this because there's no convenient way to construct a tape
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)
  def tape = loader.loadTape("multi_threaded_tape_access_spec")

  void setup() {
    tape.mode = READ_WRITE
  }

  void "the correct response is replayed to each thread"() {
    given: "a number of requests"
    List<RecordedRequest> requests = (0..<threads).collect { i ->
      new RecordedRequest.Builder()
          .url("http://example.com/$i")
          .addHeader("X-Thread", i.toString())
          .build()
    }

    and: "some existing responses on tape"
    requests.eachWithIndex { request, i ->
      def response = new RecordedResponse.Builder()
          .code(200)
          .body(ResponseBody.create(MediaType.parse("text/plain"), i.toString()))
          .build()
      tape.record(request, response)
    }

    when: "requests are replayed concurrently"
    def finished = new CountDownLatch(threads)
    def responses = [:].asSynchronized()
    requests.eachWithIndex { request, i ->
      Thread.start {
        def response = tape.play(request)
        responses[requests[i].header("X-Thread")] = response.bodyAsText()
        finished.countDown()
      }
    }

    then: "all threads complete"
    finished.await(1, SECONDS)

    and: "the correct response is returned to each request"
    responses.every { key, value ->
      key == value
    }

    where:
    threads = 10
  }

  void "each recorded response is used by just one thread when using sequential mode"() {
    given:
    tape.mode = WRITE_SEQUENTIAL

    and: "a number of requests"
    List<RecordedRequest> requests = (0..<threads).collect { i ->
      new RecordedRequest.Builder()
          .url("http://example.com/")
          .addHeader("X-Thread", i.toString())
          .build()
    }

    and: "some existing responses on tape"
    requests.eachWithIndex { request, i ->
      def response = new RecordedResponse.Builder()
          .code(200)
          .body(ResponseBody.create(MediaType.parse("text/plain"), i.toString()))
          .build()
      tape.record(request, response)
    }

    when: "requests are replayed concurrently"
    tape.mode = READ_SEQUENTIAL
    def finished = new CountDownLatch(threads)
    def responses = [].asSynchronized()
    requests.eachWithIndex { request, i ->
      Thread.start {
        def response = tape.play(request)
        responses << response.bodyAsText()
        finished.countDown()
      }
    }

    then: "all threads complete"
    finished.await(1, SECONDS)

    and: "the responses are played back sequentially and not re-used"
    responses.containsAll((0..<threads)*.toString())

    where:
    threads = 10
  }
}
