package okreplay;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.logging.Logger;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

class YamlTapeLoader implements TapeLoader<YamlTape> {
  private static final Logger LOG = Logger.getLogger(YamlTapeLoader.class.getSimpleName());
  private final TapeRoot tapeRoot;

  YamlTapeLoader(File tapeRoot) {
    this(new DefaultTapeRoot(tapeRoot));
  }

  YamlTapeLoader(TapeRoot tapeRoot) {
    this.tapeRoot = tapeRoot;
  }

  @Override public YamlTape loadTape(String tapeName) {
    String fileName = normalize(tapeName);
    if (tapeRoot.tapeExists(fileName)) {
      Reader reader = tapeRoot.readerFor(fileName);
      YamlTape tape = readFrom(reader);
      LOG.info(String.format(Locale.US,
          "loaded tape with %d recorded interactions from file %s...", tape.size(), fileName));
      return tape;
    } else {
      return newTape(tapeName);
    }
  }

  @Override public void writeTape(final Tape tape) {
    String fileName = normalize(tape.getName());
    if (tape.isDirty()) {
      //noinspection OverlyBroadCatchBlock
      try {
        Writer writer = tapeRoot.writerFor(fileName);
        LOG.info(String.format("writing tape %s to file %s...", tape.getName(), fileName));
        writeTo(tape, writer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  YamlTape newTape(String name) {
    YamlTape tape = new YamlTape();
    tape.setName(name);
    return tape;
  }

  YamlTape readFrom(Reader reader) {
    try {
      return (YamlTape) getYaml().load(reader);
    } catch (YAMLException | ClassCastException e) {
      throw new TapeLoadException("Invalid tape", e);
    }
  }

  void writeTo(Tape tape, Writer writer) throws IOException {
    try {
      getYaml().dump(tape, writer);
    } finally {
      writer.close();
    }
  }

  @Override public String normalize(String tapeName) {
    return FilenameNormalizer.toFilename(tapeName) + ".yaml";
  }

  private static Yaml getYaml() {
    Representer representer = new TapeRepresenter();
    representer.addClassTag(YamlTape.class, YamlTape.TAPE_TAG);
    Constructor constructor = new TapeConstructor();
    constructor.addTypeDescription(new TypeDescription(YamlTape.class, YamlTape.TAPE_TAG));
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(BLOCK);
    dumperOptions.setWidth(256);
    Yaml yaml = new Yaml(constructor, representer, dumperOptions);
    yaml.setBeanAccess(BeanAccess.FIELD);
    return yaml;
  }
}
