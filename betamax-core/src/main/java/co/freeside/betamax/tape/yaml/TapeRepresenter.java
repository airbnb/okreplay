/*
 * Copyright 2011 Rob Fletcher
 *
 * Converted from Groovy to Java by Sean Freitag
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

package co.freeside.betamax.tape.yaml;

import co.freeside.betamax.message.tape.RecordedRequest;
import co.freeside.betamax.message.tape.RecordedResponse;
import co.freeside.betamax.tape.RecordedInteraction;
import co.freeside.betamax.tape.Tape;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.net.URI;
import java.util.*;

/**
 * Applies a fixed ordering to properties and excludes `null` valued properties, empty collections and empty maps.
 */
public class TapeRepresenter extends GroovyRepresenter {
    public TapeRepresenter() {
        setPropertyUtils(new TapePropertyUtils());
        representers.put(URI.class, new RepresentURI());
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object bean, Property property, Object value, Tag customTag) {
        NodeTuple tuple = super.representJavaBeanProperty(bean, property, value, customTag);
        if (tuple.getValueNode().getTag().equals(Tag.NULL))
            return null;

        if (tuple.getValueNode() instanceof SequenceNode && ((SequenceNode)(tuple.getValueNode())).getValue().isEmpty())
            return null;

        if (tuple.getValueNode() instanceof MappingNode  && ((MappingNode) (tuple.getValueNode())).getValue().isEmpty())
            return null;

        if (property.getName().equals("body")) {
            ScalarNode n = (ScalarNode) tuple.getValueNode();
            if (n.getStyle() == DumperOptions.ScalarStyle.PLAIN.getChar())
                return tuple;

            return new NodeTuple(tuple.getKeyNode(), new ScalarNode(n.getTag(), n.getValue(), n.getStartMark(), n.getEndMark(), DumperOptions.ScalarStyle.LITERAL.getChar()));
        }

        return tuple;
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, Object> mapping, Boolean flowStyle) {
        return super.representMapping(tag, sort(mapping), flowStyle);
    }

    private <K, V> Map<K, V> sort(Map<K, V> self) {
        return new TreeMap<K, V>(self);
    }

    private class RepresentURI implements Represent {
        public Node representData(Object data) {
            return representScalar(Tag.STR, data.toString());
        }
    }

    public class TapePropertyUtils extends PropertyUtils {
        @Override
        protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
            try {
                Set<Property> properties = super.createPropertySet(type, bAccess);
                if (type.equals(Tape.class)) {
                    return sort(properties, "name", "interactions");
                } else if (type.equals(RecordedInteraction.class)) {
                    return sort(properties, "recorded", "request", "response");
                } else if (type.equals(RecordedRequest.class)) {
                    return sort(properties, "method", "uri", "headers", "body");
                } else if (type.equals(RecordedResponse.class)) {
                    return sort(properties, "status", "headers", "body");
                } else {
                    return properties;
                }
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }

        private Set<Property> sort(Set<Property> properties, String... names) {
            return new LinkedHashSet<Property>(Ordering.from(new OrderedPropertyComparator(names)).sortedCopy(properties));
        }
    }

    public class OrderedPropertyComparator implements Comparator<Property> {
        public OrderedPropertyComparator(String... propertyNames) {
            this.propertyNames = Lists.newArrayList(propertyNames);
        }

        public int compare(Property a, Property b) {
            return Ints.compare(propertyNames.indexOf(a.getName()), propertyNames.indexOf(b.getName()));
        }

        private List<String> propertyNames;
    }
}

