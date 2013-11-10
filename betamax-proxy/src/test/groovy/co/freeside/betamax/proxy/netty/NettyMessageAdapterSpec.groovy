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

package co.freeside.betamax.proxy.netty

import io.netty.handler.codec.http.*
import spock.lang.*
import static com.google.common.base.Charsets.*
import static com.google.common.net.MediaType.*
import static io.netty.buffer.Unpooled.*
import static io.netty.handler.codec.http.HttpHeaders.Names.*

@Unroll
abstract class NettyMessageAdapterSpec<T extends HttpMessage, A extends NettyMessageAdapter<T>> extends Specification {

    @Subject A adapter
    HttpHeaders nettyMessageHeaders = new DefaultHttpHeaders()
    T nettyMessage

    abstract void createAdapter()

    void "can read headers"() {
        given:
        nettyMessageHeaders.add(IF_NONE_MATCH, "abc123")
        nettyMessageHeaders.add(ACCEPT_ENCODING, ["gzip", "deflate"])

        and:
        createAdapter()

        expect:
        adapter.getHeader(IF_NONE_MATCH) == "abc123"
        adapter.getHeader(ACCEPT_ENCODING) == "gzip, deflate"
    }

    void "headers are immutable"() {
        given:
        createAdapter()

        when:
        adapter.headers[IF_NONE_MATCH] = ["abc123"]

        then:
        thrown UnsupportedOperationException
    }

    void "body is readable as text"() {
        given:
        nettyMessageHeaders.set(CONTENT_TYPE, FORM_DATA.withCharset(ISO_8859_1).toString())

        and:
        createAdapter()

        and:
        def chunk = new DefaultHttpContent(copiedBuffer(bodyText.getBytes(ISO_8859_1)))
        adapter.append chunk

        expect:
        adapter.hasBody()
        adapter.bodyAsText.input.text == bodyText

        where:
        bodyText = "value=\u00a31"
    }

    void "body is readable as binary"() {
        given:
        nettyMessageHeaders.set(CONTENT_TYPE, FORM_DATA.withCharset(ISO_8859_1).toString())

        and:
        createAdapter()

        and:
        def chunk = new DefaultHttpContent(copiedBuffer(body))
        adapter.append chunk

        expect:
        adapter.hasBody()
        adapter.bodyAsBinary.input.bytes == body

        where:
        body = "value=\u00a31".getBytes(ISO_8859_1)
    }

    void "headers can be appended after the adapter is created"() {
        given:
        nettyMessageHeaders.add(ACCEPT_ENCODING, "gzip")

        and:
        createAdapter()

        when:
        def laterHeaders = new DefaultHttpHeaders()
        laterHeaders.add(IF_NONE_MATCH, "abc123")
        laterHeaders.add(ACCEPT_ENCODING, "gzip")
        // duplicate should get discarded
        laterHeaders.add(ACCEPT_ENCODING, "deflate")
        def message = Stub(HttpMessage) {
            headers() >> laterHeaders
        }
        adapter.copyHeaders(message)

        then:
        adapter.getHeader(IF_NONE_MATCH) == "abc123"
        adapter.getHeader(ACCEPT_ENCODING) == "gzip, deflate"
    }

    void "multiple content chunks can be appended to the body"() {
        given:
        nettyMessageHeaders.set(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString())

        and:
        createAdapter()

        and:
        (1..3).each {
            adapter.append new DefaultHttpContent(copiedBuffer("$it".bytes))
        }

        expect:
        adapter.hasBody()
        adapter.bodyAsText.input.text == "123"
    }

    void "#description if the content buffer is #contentDescription"() {
        given:
        createAdapter()

        and:
        def chunk = new DefaultHttpContent(copiedBuffer(content))
        adapter.append chunk

        expect:
        adapter.hasBody() == consideredToHaveBody

        where:
        content                      | consideredToHaveBody
        copiedBuffer("O HAI", UTF_8) | true
        EMPTY_BUFFER                 | false

        description = consideredToHaveBody ? "has a body" : "does not have a body"
        contentDescription = content ? "${content.readableBytes()} bytes long" : "null"
    }

}
