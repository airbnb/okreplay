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

import java.io.*;
import java.util.logging.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.tape.*;
import co.freeside.betamax.tape.*;
import com.google.common.io.*;
import org.apache.tika.mime.*;
import org.yaml.snakeyaml.nodes.*;

class YamlTape extends MemoryTape implements StorableTape {

    public static final Tag TAPE_TAG = new Tag("!tape");

    private boolean dirty;
    private final File tapeRoot;
    private final MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();

    private static final Logger LOG = Logger.getLogger(YamlTape.class.getName());

    public YamlTape(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void record(Request request, Response response) {
        super.record(request, response);
        dirty = true;
    }

    @Override
    protected void writeBodyToExternal(Message message, RecordedMessage clone) throws IOException {
        File body = fileFor(message);
        ByteStreams.copy(message.getBodyAsBinary(), Files.newOutputStreamSupplier(body));
        clone.setBody(body);
    }

    private File fileFor(Message message) throws IOException {
        try {
            String suffix = mimeTypes.forName(message.getContentType()).getExtension();
            return new File(tapeRoot, String.format("body%s", suffix));
        } catch (MimeTypeException e) {
            LOG.warning(String.format("Could not get extension for %s content type: %s", message.getContentType(), e.getMessage()));
            return File.createTempFile("body", ".bin");
        }
    }

}
