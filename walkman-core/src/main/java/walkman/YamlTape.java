package walkman;

import org.yaml.snakeyaml.nodes.Tag;

import walkman.Request;
import walkman.Response;
import walkman.MemoryTape;

class YamlTape extends MemoryTape {
  static final Tag TAPE_TAG = new Tag("!tape");
  static final Tag FILE_TAG = new Tag("!file");

  private transient boolean dirty;

  @Override public boolean isDirty() {
    return dirty;
  }

  @Override public void record(Request request, Response response) {
    super.record(request, response);
    dirty = true;
  }
}
