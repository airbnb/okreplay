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

package co.freeside.betamax.io

import com.google.common.io.Files
import spock.lang.*

class FileResolverSpec extends Specification {

    @Shared
    @AutoCleanup("deleteDir")
    def baseDirectory = Files.createTempDir()
    @Subject
    def fileResolver = new FileResolver(baseDirectory)

    @Unroll("converts between #absolutePath and #path using base directory")
    void "converts between file and a path relative to the base directory"() {
        expect:
        fileResolver.toPath(file) == path

        and:
        fileResolver.toFile(path) == file

        where:
        file                                   | path
        new File(baseDirectory, "foo.txt")     | "foo.txt"
        new File(baseDirectory, "foo/bar.txt") | "foo/bar.txt"

        absolutePath = file.absolutePath
    }

    void "does not handle files that are not in the base directory tree"() {
        when:
        fileResolver.toPath(new File(baseDirectory.parentFile, "foo.txt"))

        then:
        thrown IllegalArgumentException
    }

}
