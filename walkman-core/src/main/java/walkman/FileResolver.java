package walkman;

import com.google.common.base.Joiner;

import java.io.File;

/**
 * Converts between {@link File} instances and a path relative to a known base
 * directory.
 */
public final class FileResolver {
  private static final Joiner PATH_JOINER = Joiner.on(File.separatorChar);
  private final File baseDirectory;
  private final String baseDirectoryPath;

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
      throw new IllegalArgumentException("file is not in the base directory sub-tree of this " +
          "FileResolver");
    }
    return absolutePath.substring(baseDirectoryPath.length() + 1);
  }
}
