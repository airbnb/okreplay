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

package co.freeside.betamax.tck

import java.util.concurrent.CountDownLatch
import co.freeside.betamax.junit.Betamax
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static java.util.concurrent.TimeUnit.SECONDS

abstract class MultiThreadedTapeWritingSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()

    @Shared @AutoCleanup("stop") SimpleServer endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    @Betamax(tape = "multi_threaded_tape_writing_spec", mode = READ_WRITE)
    void "the tape can cope with concurrent reading and writing"() {
        when: "requests are fired concurrently"
        def finished = new CountDownLatch(threads)
        def responses = Collections.synchronizedMap([:])
        threads.times { i ->
            Thread.start {
                try {
                    responses[i.toString()] = makeRequest("$endpoint.url$i")
                } catch (Exception e) {
                    responses[i.toString()] = "FAIL!"
                }
                finished.countDown()
            }
        }

        then: "all threads complete"
        finished.await(10, SECONDS)
        responses.size() == threads

        and: "the correct response is returned to each request"
        responses.every { Map.Entry<String, String> it ->
            it.value.startsWith("GET /$it.key")
        }

        where:
        threads = 10
    }

    protected abstract String makeRequest(String url)
}
