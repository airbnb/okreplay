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

package co.freeside.betamax.io

import spock.lang.*
import static com.google.common.net.MediaType.*

@Unroll
@Issue("https://github.com/robfletcher/betamax/issues/53")
class ContentTypesSpec extends Specification {

    void "identifies #mimeType as #type"() {
        expect:
        ContentTypes.isTextContentType(mimeType) ^ isBinary

        where:
        mimeType                 | isBinary
        null                     | true
        "text/plain"             | false
        "application/json"       | false
        "application/javascript" | false
        "application/xml"        | false
        "application/rss+xml"    | false
        "application/atom+xml"   | false
        "application/rdf+xml"    | false
        "image/png"              | true
        FORM_DATA.toString()     | false
        JSON_UTF_8.toString()    | false

        type = isBinary ? "binary" : "text"
    }

}
