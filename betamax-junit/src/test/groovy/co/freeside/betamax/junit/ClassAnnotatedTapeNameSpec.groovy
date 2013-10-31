/*
 * Copyright 2013 the original author or authors.
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

package co.freeside.betamax.junit

import co.freeside.betamax.Configuration
import com.google.common.io.Files
import org.junit.ClassRule
import spock.lang.*

@Betamax
class ClassAnnotatedTapeNameSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def TAPE_ROOT = Files.createTempDir()
    @Shared def configuration = Configuration.builder().tapeRoot(TAPE_ROOT).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    void "tape name defaults to class name"() {
        expect:
        recorder.tape.name == "class annotated tape name spec"
    }

}
