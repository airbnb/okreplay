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
