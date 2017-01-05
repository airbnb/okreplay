/*
 * Copyright 2013 the original author or authors.
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

package software.betamax.tape.yaml;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;
import software.betamax.io.FileResolver;

public class TapeConstructor extends Constructor {

    public TapeConstructor(FileResolver fileResolver) {
        yamlClassConstructors.put(NodeId.mapping, new ConstructTape());
        yamlConstructors.put(YamlTape.FILE_TAG, new ConstructFile(fileResolver));
    }

    private class ConstructTape extends ConstructMapping {
        @Override
        protected Object createEmptyJavaBean(MappingNode node) {
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

        @Override
        public Object construct(Node node) {
            String path = (String) constructScalar((ScalarNode) node);
            return fileResolver.toFile(path);
        }
    }
}
