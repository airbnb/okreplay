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

package co.freeside.betamax.io;

import java.util.logging.Logger;
import org.apache.tika.mime.*;

/**
 * Maps *MIME* types to file extensions.
 */
public class FileTypeMapper {

    /**
     * The extension applied to a file name if the appropriate extension for a
     * MIME type is not known.
     */
    public static final String DEFAULT_EXTENSION = ".data";

    private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();

    private static final Logger LOG = Logger.getLogger(FileTypeMapper.class.getName());

    /**
     * Returns a filename consisting of `baseName` and an appropriate file
     * extension for the specified MIME type.
     *
     * @param baseName    a base file name.
     * @param contentType a MIME content type such as `text/plain`.
     * @return a filename with an appropriate extension.
     */
    public static String filenameFor(String baseName, String contentType) {
        String filename;
        try {
            String extension = MIME_TYPES.forName(contentType).getExtension();
            filename = baseName + extension;
        } catch (MimeTypeException e) {
            LOG.warning(String.format("Could not get extension for %s content type: %s", contentType, e.getMessage()));
            filename = baseName + DEFAULT_EXTENSION;
        }
        return filename;
    }

    private FileTypeMapper() {
    }
}
