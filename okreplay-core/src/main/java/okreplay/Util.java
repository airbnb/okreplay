package okreplay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import javax.annotation.Nullable;

import static java.lang.String.format;

final class Util {
  static final String VIA = "Via";
  static final String CONTENT_TYPE = "Content-Type";

  static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  static int compare(int a, int b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  static int checkPositionIndex(int index, int size) {
    return checkPositionIndex(index, size, "index");
  }

  static int checkPositionIndex(int index, int size, @Nullable String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
    }
    return index;
  }

  static String badPositionIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index > size
      return format("%s (%s) must not be greater than size (%s)", desc, index, size);
    }
  }

  static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  static BufferedReader newReader(File file, Charset charset) throws FileNotFoundException {
    checkNotNull(file);
    checkNotNull(charset);
    return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
  }

  static BufferedWriter newWriter(File file, Charset charset) throws FileNotFoundException {
    checkNotNull(file);
    checkNotNull(charset);
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
  }

  static <T> int indexOf(Iterator<T> iterator, Predicate<? super T> predicate) {
    checkNotNull(predicate, "predicate");
    for (int i = 0; iterator.hasNext(); i++) {
      T current = iterator.next();
      if (predicate.apply(current)) {
        return i;
      }
    }
    return -1;
  }

  static <T> boolean all(Iterator<T> iterator, Predicate<? super T> predicate) {
    checkNotNull(predicate);
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (!predicate.apply(element)) {
        return false;
      }
    }
    return true;
  }
}
