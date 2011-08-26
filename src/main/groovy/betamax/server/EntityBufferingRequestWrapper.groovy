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

package betamax.server

import groovy.util.logging.Log4j
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.EntityEnclosingRequestWrapper
import org.apache.http.*

@Log4j
class EntityBufferingRequestWrapper extends EntityEnclosingRequestWrapper {

	EntityBufferingRequestWrapper(HttpEntityEnclosingRequest request) {
		super(request)
	}

	@Override
	void setEntity(HttpEntity entity) {
		if (entity.isRepeatable()) {
			super.setEntity(entity)
		} else {
			log.debug "buffering non-repeatable request entity..."
			def bufferedEntity = new ByteArrayEntity(entity.content.bytes)
			bufferedEntity.chunked = entity.chunked
			bufferedEntity.contentEncoding = entity.contentEncoding
			bufferedEntity.contentType = entity.contentType
			super.setEntity(bufferedEntity)
		}
	}

}
