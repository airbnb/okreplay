package betamax.storage.yaml

import betamax.storage.Tape
import org.yaml.snakeyaml.introspector.*
import betamax.storage.TapeInteraction
import betamax.storage.TapeResponse
import betamax.storage.TapeRequest
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.nodes.CollectionNode
import org.yaml.snakeyaml.nodes.Node

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
            case TapeInteraction:
                return sort(properties, ["recorded", "request", "response"])
            case TapeRequest:
                return sort(properties, ["protocol", "method", "uri", "headers", "body"])
            case TapeResponse:
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