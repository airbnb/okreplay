package betamax.storage.yaml

import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.representer.Representer

/**
 * Ensures `metaClass` property is not dumped to YAML.
 */
class GroovyRepresenter extends Representer {
	@Override
	protected Set<Property> getProperties(Class<? extends Object> type) {
		def set = super.getProperties(type)
		set.removeAll {
			it.name == "metaClass"
		}
		set
	}

}
