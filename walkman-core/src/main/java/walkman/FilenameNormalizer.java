package walkman;

import java.text.Normalizer;

public final class FilenameNormalizer {
  public static String toFilename(String tapeName) {
    return Normalizer.normalize(tapeName, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("[^\\w\\d]+", "_")
        .replaceFirst("^_", "")
        .replaceFirst("_$", "");
  }

  private FilenameNormalizer() {
  }
}
