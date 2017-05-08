package okreplay;

import org.yaml.snakeyaml.nodes.Tag;

class YamlTape extends MemoryTape {
  static final Tag TAPE_TAG = new Tag("!tape");

  private transient boolean dirty;

  @Override public boolean isDirty() {
    return dirty;
  }

  @Override public void record(Request request, Response response) {
    super.record(request, response);
    dirty = true;
  }
}
