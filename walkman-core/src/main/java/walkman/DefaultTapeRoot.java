package walkman;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public class DefaultTapeRoot implements TapeRoot {
  private static final String FILE_CHARSET = "UTF-8";
  protected final File root;

  public DefaultTapeRoot(File root) {
    this.root = root;
  }

  @Override public Reader readerFor(String tapePath) {
    File file = new File(root, tapePath);
    try {
      return Files.newReader(file, Charset.forName(FILE_CHARSET));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public Writer writerFor(String tapePath) {
    File file = new File(root, tapePath);
    //noinspection ResultOfMethodCallIgnored
    file.getParentFile().mkdirs();
    try {
      return Files.newWriter(file, Charset.forName(FILE_CHARSET));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public boolean tapeExists(String tapePath) {
    return new File(root, tapePath).isFile();
  }

  @Override public File get() {
    return root;
  }
}
