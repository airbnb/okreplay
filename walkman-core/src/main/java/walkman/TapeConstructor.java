package walkman;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class TapeConstructor extends Constructor {
  public TapeConstructor(FileResolver fileResolver) {
    yamlClassConstructors.put(NodeId.mapping, new ConstructTape());
    yamlConstructors.put(YamlTape.FILE_TAG, new ConstructFile(fileResolver));
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

  private class ConstructFile extends AbstractConstruct {

    private final FileResolver fileResolver;

    private ConstructFile(FileResolver fileResolver) {
      this.fileResolver = fileResolver;
    }

    @Override public Object construct(Node node) {
      String path = (String) constructScalar((ScalarNode) node);
      return fileResolver.toFile(path);
    }
  }
}
