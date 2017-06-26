package okreplay;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static okreplay.Util.checkNotNull;

final class Present<T> extends Optional<T> {
  private final T reference;

  Present(T reference) {
    this.reference = reference;
  }

  @Override boolean isPresent() {
    return true;
  }

  @Override T get() {
    return reference;
  }

  @Override T or(T defaultValue) {
    checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
    return reference;
  }

  @Override Optional<T> or(Optional<? extends T> secondChoice) {
    checkNotNull(secondChoice);
    return this;
  }

  @Override T orNull() {
    return reference;
  }

  @Override Set<T> asSet() {
    return Collections.singleton(reference);
  }

  @Override <V> Optional<V> transform(Function<? super T, V> function) {
    return new Present<>(
        checkNotNull(
            function.apply(reference),
            "the Function passed to Optional.transform() must not return null."));
  }

  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof Present) {
      Present<?> other = (Present<?>) object;
      return reference.equals(other.reference);
    }
    return false;
  }

  @Override public int hashCode() {
    return 0x598df91c + reference.hashCode();
  }

  @Override public String toString() {
    return "Optional.of(" + reference + ")";
  }

  private static final long serialVersionUID = 0;
}