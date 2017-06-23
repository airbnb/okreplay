package okreplay;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import static okreplay.Util.checkNotNull;

final class Absent<T> extends Optional<T> {
  private static final Absent<Object> INSTANCE = new Absent<>();

  @SuppressWarnings("unchecked") // implementation is "fully variant"
  static <T> Optional<T> withType() {
    return (Optional<T>) INSTANCE;
  }

  private Absent() {}

  @Override
  boolean isPresent() {
    return false;
  }

  @Override
  T get() {
    throw new IllegalStateException("Optional.get() cannot be called on an absent value");
  }

  @Override
  T or(T defaultValue) {
    return checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
  }

  @SuppressWarnings("unchecked") // safe covariant cast
  @Override Optional<T> or(Optional<? extends T> secondChoice) {
    return (Optional<T>) checkNotNull(secondChoice);
  }

  @Override @Nullable T orNull() { return null;
  }

  @Override
  Set<T> asSet() {
    return Collections.emptySet();
  }

  @Override
  <V> Optional<V> transform(Function<? super T, V> function) {
    checkNotNull(function);
    return Optional.absent();
  }

  @Override public boolean equals(@Nullable Object object) {
    return object == this;
  }

  @Override public int hashCode() {
    return 0x79a31aac;
  }

  @Override public String toString() {
    return "Optional.absent()";
  }

  private Object readResolve() {
    return INSTANCE;
  }

  private static final long serialVersionUID = 0;
}