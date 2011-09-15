/*
 * Copyright 2011 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package betamax.proxy

interface Response extends Message {

	/**
	 * @return the HTTP status code of the response.
	 */
    int getStatus()

    void setStatus(int status)

	void setError(int status, String reason)

	/**
	 * Adds a header to this response. Multiple headers with the same name can be added.
	 * @param name the header name.
	 * @param value the header value.
	 */
	void addHeader(String name, String value)

	/**
	 * @return the content MIME type of the response.
	 */
    String getContentType()

	/**
	 * Returns a stream that can be used to write data to the response body. If the response body should be encoded then
	 * this method must return an `OutputStream` implementation that will handle the encoding.
	 * @return a stream for writing data to the response body.
	 */
    OutputStream getOutputStream()

	/**
	 * Returns a `Writer` that can be used to write text to the response body. If the response body should be encoded
	 * then this method must return a `Writer` implementation that will handle the encoding.
	 * @return a writer for writing text to the response body.
	 */
    Writer getWriter()

}