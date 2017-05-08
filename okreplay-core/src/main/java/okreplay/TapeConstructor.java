package okreplay;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;

class TapeConstructor extends Constructor {
  TapeConstructor() {
    yamlClassConstructors.put(NodeId.mapping, new ConstructTape());
  }

  private class ConstructTape extends ConstructMapping {
    @Override protected Object createEmptyJavaBean(MappingNode node) {
      if (YamlTape.class.equals(node.getType())) {
        return new YamlTape();
      } else {
        return super.createEmptyJavaBean(node);
      }
    }
  }
}
