package okreplay;

import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Applies a fixed ordering to properties and excludes `null` valued
 * properties, empty collections and empty maps.
 */
class TapeRepresenter extends Representer {
  TapeRepresenter() {
    setPropertyUtils(new TapePropertyUtils());
    representers.put(URI.class, new RepresentURI());
  }

  @Override
  protected NodeTuple representJavaBeanProperty(Object bean, Property property, Object value, Tag
      customTag) {
    NodeTuple tuple = super.representJavaBeanProperty(bean, property, value, customTag);

    if (isNullValue(tuple) || isEmptySequence(tuple) || isEmptyMapping(tuple)) {
      return null;
    }

    return tuple;
  }

  @Override protected Node representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle) {
    return super.representMapping(tag, sort(mapping), flowStyle);
  }

  private <K, V> Map<K, V> sort(Map<K, V> self) {
    return new TreeMap<>(self);
  }

  private boolean isNullValue(NodeTuple tuple) {
    return tuple.getValueNode().getTag().equals(Tag.NULL);
  }

  private boolean isEmptySequence(NodeTuple tuple) {
    return tuple.getValueNode() instanceof SequenceNode && ((SequenceNode) tuple.getValueNode())
        .getValue().isEmpty();
  }

  private boolean isEmptyMapping(NodeTuple tuple) {
    return tuple.getValueNode() instanceof MappingNode && ((MappingNode) tuple.getValueNode())
        .getValue().isEmpty();
  }

  private class RepresentURI implements Represent {
    public Node representData(Object data) {
      return representScalar(Tag.STR, data.toString());
    }
  }

  private class TapePropertyUtils extends PropertyUtils {
    @Override protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
      try {
        Set<Property> properties = super.createPropertySet(type, bAccess);
        if (Tape.class.isAssignableFrom(type)) {
          return sort(properties, "name", "interactions");
        } else if (YamlRecordedInteraction.class.isAssignableFrom(type)) {
          return sort(properties, "recorded", "request", "response");
        } else if (YamlRecordedRequest.class.isAssignableFrom(type)) {
          return sort(properties, "method", "uri", "headers", "body");
        } else if (YamlRecordedResponse.class.isAssignableFrom(type)) {
          return sort(properties, "status", "headers", "body");
        } else {
          return properties;
        }
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }

    private Set<Property> sort(Set<Property> properties, String... names) {
      List<Property> list = new ArrayList<>(properties);
      Collections.sort(list, OrderedPropertyComparator.forNames(names));
      return new LinkedHashSet<>(list);
    }
  }

  private static class OrderedPropertyComparator implements Comparator<Property> {
    private final List<String> propertyNames;

    static OrderedPropertyComparator forNames(String... propertyNames) {
      return new OrderedPropertyComparator(propertyNames);
    }

    private OrderedPropertyComparator(String... propertyNames) {
      this.propertyNames = Arrays.asList(propertyNames);
    }

    @Override public int compare(Property a, Property b) {
      return Util.compare(propertyNames.indexOf(a.getName()), propertyNames.indexOf(b.getName()));
    }
  }
}
