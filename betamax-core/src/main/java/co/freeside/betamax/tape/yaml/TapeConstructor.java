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

package co.freeside.betamax.tape.yaml;

import co.freeside.betamax.io.FileResolver;
import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.nodes.*;
import static co.freeside.betamax.tape.yaml.YamlTape.FILE_TAG;

public class TapeConstructor extends Constructor {

    public TapeConstructor(FileResolver fileResolver) {
        yamlClassConstructors.put(NodeId.mapping, new ConstructTape(fileResolver));
        yamlConstructors.put(FILE_TAG, new ConstructFile(fileResolver));
    }

    private class ConstructTape extends ConstructMapping {

        private final FileResolver fileResolver;

        public ConstructTape(FileResolver fileResolver) {
            this.fileResolver = fileResolver;
        }

        @Override
        protected Object createEmptyJavaBean(MappingNode node) {
            if (YamlTape.class.equals(node.getType())) {
                return new YamlTape(fileResolver);
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
