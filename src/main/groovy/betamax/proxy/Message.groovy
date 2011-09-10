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

/**
 * An abstraction of an HTTP request or response. Implementations can be backed by any sort of underlying implementation.
 */
interface Message {

	/**
	 * @return all HTTP headers attached to this message.
	 */
    Map<String, List<String>> getHeaders()

	/**
	 * @param name an HTTP header name.
	 * @return values for all HTTP headers with the specified name or an empty list if there are no headers with that name.
	 */
    List<String> getHeaders(String name)

	/**
	 * @param name an HTTP header name.
	 * @return the value for the first HTTP header with the specified name or `null` if there are no headers with that name.
	 */
    String getFirstHeader(String name)

    void addHeader(String name, String value)

	boolean hasBody()
    Reader getBodyAsText()
    InputStream getBodyAsBinary()

}