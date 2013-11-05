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

import java.io.File;
import com.google.common.base.Joiner;

/**
 * Converts between {@link File} instances and a path relative to a known base
 * directory.
 */
public final class FileResolver {

    private final File baseDirectory;
    private final String baseDirectoryPath;

    static final Joiner PATH_JOINER = Joiner.on(File.separatorChar);

    public FileResolver(File baseDirectory) {
        this.baseDirectory = baseDirectory.getAbsoluteFile();
        baseDirectoryPath = baseDirectory.getAbsolutePath();
    }

    public File toFile(String... path) {
        return new File(baseDirectory, PATH_JOINER.join(path));
    }

    public String toPath(File file) {
        String absolutePath = file.getAbsolutePath();
        if (!absolutePath.startsWith(baseDirectoryPath)) {
            throw new IllegalArgumentException("file is not in the base directory sub-tree of this FileResolver");
        }
        return absolutePath.substring(baseDirectoryPath.length() + 1);
    }

}
