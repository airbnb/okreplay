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

package betamax.tape.yaml

import betamax.Tape
import org.yaml.snakeyaml.representer.Represent
import betamax.tape.*
import static org.yaml.snakeyaml.DumperOptions.ScalarStyle.*
import org.yaml.snakeyaml.introspector.*
import org.yaml.snakeyaml.nodes.*
import static org.yaml.snakeyaml.nodes.Tag.*

/**
 * Applies a fixed ordering to properties and excludes `null` valued properties, empty collections and empty maps.
 */
class TapeRepresenter extends GroovyRepresenter {

	TapeRepresenter() {
		propertyUtils = new TapePropertyUtils()
		representers[URI] = new RepresentURI()
	}

	@Override
	protected NodeTuple representJavaBeanProperty(Object bean, Property property, Object value, Tag customTag) {
		def tuple = super.representJavaBeanProperty(bean, property, value, customTag)
		if (tuple.valueNode.tag == NULL) {
			null
		} else if (tuple.valueNode instanceof CollectionNode && tuple.valueNode.value.empty) {
			null
		} else if (property.name == "body") {
			ScalarNode n = tuple.valueNode
			if (n.style == PLAIN.char) {
				tuple
			} else {
				new NodeTuple(tuple.keyNode, new ScalarNode(n.tag, n.value, n.startMark, n.endMark, LITERAL.char))
			}
		} else {
			tuple
		}
	}

	@Override
	protected Node representMapping(Tag tag, Map<? extends Object, Object> mapping, Boolean flowStyle) {
		super.representMapping(tag, mapping.sort(), flowStyle)
	}

	private class RepresentURI implements Represent {
		Node representData(Object data) {
			representScalar STR, data.toString()
		}
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
				return sort(properties, ["method", "uri", "headers", "body"])
			case RecordedResponse:
				return sort(properties, ["status", "headers", "body"])
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