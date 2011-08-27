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

package betamax.storage.yaml

import betamax.Tape

import org.yaml.snakeyaml.introspector.*
import org.yaml.snakeyaml.nodes.*
import betamax.tape.RecordedResponse
import betamax.tape.RecordedRequest
import betamax.tape.RecordedInteraction

/**
 * Applies a fixed ordering to properties and excludes `null` valued properties, empty collections and empty maps.
 */
class TapeRepresenter extends GroovyRepresenter {

	TapeRepresenter() {
		propertyUtils = new TapePropertyUtils()
	}

	@Override
	protected NodeTuple representJavaBeanProperty(Object bean, Property property, Object value, Tag customTag) {
		def tuple = super.representJavaBeanProperty(bean, property, value, customTag)
		if (Tag.NULL == tuple.valueNode.tag) {
			null
		} else if (tuple.valueNode instanceof CollectionNode && tuple.valueNode.value.empty) {
			null
		} else {
			tuple
		}
	}

	@Override
	protected Node representMapping(Tag tag, Map<? extends Object, Object> mapping, Boolean flowStyle) {
		super.representMapping(tag, mapping.sort(), flowStyle)
	}

}

class TapePropertyUtils extends PropertyUtils {
	@Override
	protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
		def properties = super.createPropertySet(type, bAccess)
		switch (type) {
			case Tape:
				return sort(properties, ["name", "interactions"])
			case RecordedInteraction:
				return sort(properties, ["recorded", "request", "response"])
			case RecordedRequest:
				return sort(properties, ["protocol", "method", "uri", "headers", "body"])
			case RecordedResponse:
				return sort(properties, ["protocol", "status", "headers", "body"])
			default:
				return properties
		}
	}

	private Set<Property> sort(Set<Property> properties, List<String> names) {
		return new LinkedHashSet(properties.sort(new OrderedPropertyComparator(names)))
	}
}

class OrderedPropertyComparator implements Comparator<Property> {

	private List<String> propertyNames

	OrderedPropertyComparator(List<String> propertyNames) {
		this.propertyNames = propertyNames.asImmutable()
	}

	int compare(Property a, Property b) {
		propertyNames.indexOf(a.name) <=> propertyNames.indexOf(b.name)
	}

}