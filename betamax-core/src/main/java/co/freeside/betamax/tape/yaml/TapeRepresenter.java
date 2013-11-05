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

package co.freeside.betamax.tape.yaml;

import java.beans.IntrospectionException;
import java.io.File;
import java.net.URI;
import java.util.*;
import co.freeside.betamax.io.FileResolver;
import co.freeside.betamax.message.tape.*;
import co.freeside.betamax.tape.*;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import org.yaml.snakeyaml.introspector.*;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.*;
import static org.yaml.snakeyaml.DumperOptions.ScalarStyle.*;

/**
 * Applies a fixed ordering to properties and excludes `null` valued
 * properties, empty collections and empty maps.
 */
public class TapeRepresenter extends Representer {

    public TapeRepresenter(FileResolver fileResolver) {
        setPropertyUtils(new TapePropertyUtils());
        representers.put(URI.class, new RepresentURI());
        representers.put(File.class, new RepresentFile(fileResolver));
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object bean, Property property, Object value, Tag customTag) {
        NodeTuple tuple = super.representJavaBeanProperty(bean, property, value, customTag);

        if (isNullValue(tuple) || isEmptySequence(tuple) || isEmptyMapping(tuple)) {
            return null;
        }

        if ("body".equals(property.getName())) {
            ScalarNode n = (ScalarNode) tuple.getValueNode();
            if (n.getStyle() == PLAIN.getChar()) {
                return tuple;
            } else {
                return new NodeTuple(tuple.getKeyNode(), new ScalarNode(n.getTag(), n.getValue(), n.getStartMark(), n.getEndMark(), LITERAL.getChar()));
            }
        }

        return tuple;
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle) {
        return super.representMapping(tag, sort(mapping), flowStyle);
    }

    private <K, V> Map<K, V> sort(Map<K, V> self) {
        return new TreeMap<K, V>(self);
    }

    private boolean isNullValue(NodeTuple tuple) {
        return tuple.getValueNode().getTag().equals(Tag.NULL);
    }

    private boolean isEmptySequence(NodeTuple tuple) {
        return tuple.getValueNode() instanceof SequenceNode && ((SequenceNode) tuple.getValueNode()).getValue().isEmpty();
    }

    private boolean isEmptyMapping(NodeTuple tuple) {
        return tuple.getValueNode() instanceof MappingNode && ((MappingNode) tuple.getValueNode()).getValue().isEmpty();
    }

    private class RepresentURI implements Represent {
        public Node representData(Object data) {
            return representScalar(Tag.STR, data.toString());
        }
    }

    private class RepresentFile implements Represent {

        private final FileResolver fileResolver;

        public RepresentFile(FileResolver fileResolver) {
            this.fileResolver = fileResolver;
        }

        @Override
        public Node representData(Object data) {
            return representScalar(YamlTape.FILE_TAG, fileResolver.toPath((File) data));
        }
    }

    public class TapePropertyUtils extends PropertyUtils {
        @Override
        protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
            try {
                Set<Property> properties = super.createPropertySet(type, bAccess);
                if (Tape.class.isAssignableFrom(type)) {
                    return sort(properties, "name", "interactions");
                } else if (RecordedInteraction.class.isAssignableFrom(type)) {
                    return sort(properties, "recorded", "request", "response");
                } else if (RecordedRequest.class.isAssignableFrom(type)) {
                    return sort(properties, "method", "uri", "headers", "body");
                } else if (RecordedResponse.class.isAssignableFrom(type)) {
                    return sort(properties, "status", "headers", "body");
                } else {
                    return properties;
                }
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }

        private Set<Property> sort(Set<Property> properties, String... names) {
            return Sets.newLinkedHashSet(Ordering.from(OrderedPropertyComparator.forNames(names)).sortedCopy(properties));
        }
    }

    public static class OrderedPropertyComparator implements Comparator<Property> {

        public static OrderedPropertyComparator forNames(String... propertyNames) {
            return new OrderedPropertyComparator(propertyNames);
        }

        private OrderedPropertyComparator(String... propertyNames) {
            this.propertyNames = Arrays.asList(propertyNames);
        }

        public int compare(Property a, Property b) {
            return Ints.compare(propertyNames.indexOf(a.getName()), propertyNames.indexOf(b.getName()));
        }

        private List<String> propertyNames;
    }
}

