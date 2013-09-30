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

package co.freeside.betamax.util

@Category(Properties)
class PropertiesCategory {

	boolean getBoolean(String key, boolean defaultValue = false) {
		def value = this.getProperty(key)
		value ? value.toBoolean() : defaultValue
	}

	int getInteger(String key, int defaultValue = 0) {
		def value = this.getProperty(key)
		value ? value.toInteger() : defaultValue
	}

	public <T> T getEnum(String key, T defaultValue) {
		def value = this.getProperty(key)
		value ? Enum.valueOf(defaultValue.getClass(), value) : defaultValue
	}

}
