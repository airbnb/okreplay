package okreplay;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import static okreplay.Util.checkNotNull;

abstract class Optional<T> implements Serializable {
  /**
   * Returns an {@code Optional} instance with no contained reference.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method is equivalent to Java 8's
   * {@code Optional.empty}.
   */
  static <T> Optional<T> absent() {
    return Absent.withType();
  }

  /**
   * Returns an {@code Optional} instance containing the given non-null reference. To have {@code
   * null} treated as {@link #absent}, use {@link #fromNullable} instead.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> no differences.
   *
   * @throws NullPointerException if {@code reference} is null
   */
  static <T> Optional<T> of(T reference) {
    return new Present<T>(checkNotNull(reference));
  }

  /**
   * If {@code nullableReference} is non-null, returns an {@code Optional} instance containing that
   * reference; otherwise returns {@link Optional#absent}.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method is equivalent to Java 8's
   * {@code Optional.ofNullable}.
   */
  static <T> Optional<T> fromNullable(@Nullable T nullableReference) {
    return (nullableReference == null)
        ? Optional.<T>absent()
        : new Present<T>(nullableReference);
  }

  Optional() {
  }

  /**
   * Returns {@code true} if this holder contains a (non-null) instance.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> no differences.
   */
  abstract boolean isPresent();

  /**
   * Returns the contained instance, which must be present. If the instance might be absent, use
   * {@link #or(Object)} or {@link #orNull} instead.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> when the value is absent, this method
   * throws {@link IllegalStateException}, whereas the Java 8 counterpart throws
   * {@link java.util.NoSuchElementException NoSuchElementException}.
   *
   * @throws IllegalStateException if the instance is absent ({@link #isPresent} returns
   *                               {@code false}); depending on this <i>specific</i> exception
   *                               type (over the more general
   *                               {@link RuntimeException}) is discouraged
   */
  abstract T get();

  /**
   * Returns the contained instance if it is present; {@code defaultValue} otherwise. If no default
   * value should be required because the instance is known to be present, use {@link #get()}
   * instead. For a default value of {@code null}, use {@link #orNull}.
   * <p>
   * <p>Note about generics: The signature {@code T or(T defaultValue)} is overly
   * restrictive. However, the ideal signature, {@code <S super T> S or(S)}, is not legal
   * Java. As a result, some sensible operations involving subtypes are compile errors:
   * <pre>   {@code
   *
   *   Optional<Integer> optionalInt = getSomeOptionalInt();
   *   Number value = optionalInt.or(0.5); // error
   *
   *   FluentIterable<? extends Number> numbers = getSomeNumbers();
   *   Optional<? extends Number> first = numbers.first();
   *   Number value = first.or(0.5); // error}</pre>
   * <p>
   * <p>As a workaround, it is always safe to cast an {@code Optional<? extends T>} to {@code
   * Optional<T>}. Casting either of the above example {@code Optional} instances to {@code
   * Optional<Number>} (where {@code Number} is the desired output type) solves the problem:
   * <pre>   {@code
   *
   *   Optional<Number> optionalInt = (Optional) getSomeOptionalInt();
   *   Number value = optionalInt.or(0.5); // fine
   *
   *   FluentIterable<? extends Number> numbers = getSomeNumbers();
   *   Optional<Number> first = (Optional) numbers.first();
   *   Number value = first.or(0.5); // fine}</pre>
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method is similar to Java 8's
   * {@code Optional.orElse}, but will not accept {@code null} as a {@code defaultValue}
   * ({@link #orNull} must be used instead). As a result, the value returned by this method is
   * guaranteed non-null, which is not the case for the {@code java.util} equivalent.
   */
  abstract T or(T defaultValue);

  /**
   * Returns this {@code Optional} if it has a value present; {@code secondChoice} otherwise.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method has no equivalent in Java 8's
   * {@code Optional} class; write {@code thisOptional.isPresent() ? thisOptional : secondChoice}
   * instead.
   */
  abstract Optional<T> or(Optional<? extends T> secondChoice);

  /**
   * Returns the contained instance if it is present; {@code null} otherwise. If the instance is
   * known to be present, use {@link #get()} instead.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method is equivalent to Java 8's
   * {@code Optional.orElse(null)}.
   */
  @Nullable abstract T orNull();

  /**
   * Returns an immutable singleton {@link Set} whose only element is the contained instance if it
   * is present; an empty immutable {@link Set} otherwise.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method has no equivalent in Java 8's
   * {@code Optional} class. However, this common usage: <pre>   {@code
   * <p>
   *   for (Foo foo : possibleFoo.asSet()) {
   *     doSomethingWith(foo);
   *   }}</pre>
   * <p>
   * ... can be replaced with: <pre>   {@code
   * <p>
   *   possibleFoo.ifPresent(foo -> doSomethingWith(foo));}</pre>
   *
   * @since 11.0
   */
  abstract Set<T> asSet();

  /**
   * If the instance is present, it is transformed with the given {@link Function}; otherwise,
   * {@link Optional#absent} is returned.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this method is similar to Java 8's
   * {@code Optional.map}, except when {@code function} returns {@code null}. In this case this
   * method throws an exception, whereas the Java 8 method returns {@code Optional.absent()}.
   *
   * @throws NullPointerException if the function returns {@code null}
   * @since 12.0
   */
  abstract <V> Optional<V> transform(Function<? super T, V> function);

  /**
   * Returns {@code true} if {@code object} is an {@code Optional} instance, and either the
   * contained references are {@linkplain Object#equals equal} to each other or both are absent.
   * Note that {@code Optional} instances of differing parameterized types can be equal.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> no differences.
   */
  @Override public abstract boolean equals(@Nullable Object object);

  /**
   * Returns a hash code for this instance.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this class leaves the specific choice of
   * hash code unspecified, unlike the Java 8 equivalent.
   */
  @Override public abstract int hashCode();

  /**
   * Returns a string representation for this instance.
   * <p>
   * <p><b>Comparison to {@code java.util.Optional}:</b> this class leaves the specific string
   * representation unspecified, unlike the Java 8 equivalent.
   */
  @Override public abstract String toString();

  private static final long serialVersionUID = 0;
}