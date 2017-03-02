package walkman;

import java.io.File;

class DefaultTapeRoot implements TapeRoot {
  private final File file;

  DefaultTapeRoot(File file) {
    this.file = file;
  }

  @Override public File get() {
    return file;
  }
}
