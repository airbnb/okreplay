package okreplay

import java.io.Serializable
import java.util.function.Function

import okreplay.Util.checkNotNull

abstract class Optional<T> : Serializable {

  /**
   * Returns `true` if this holder contains a (non-null) instance.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** no differences.
   */
  internal abstract val isPresent: Boolean

  /**
   * Returns the contained instance, which must be present. If the instance might be absent, use
   * [.or] or [.orNull] instead.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** when the value is absent, this method
   * throws [IllegalStateException], whereas the Java 8 counterpart throws
   * [NoSuchElementException][java.util.NoSuchElementException].
   *
   * @throws IllegalStateException if the instance is absent ([.isPresent] returns
   * `false`); depending on this *specific* exception
   * type (over the more general
   * [RuntimeException]) is discouraged
   */
  internal abstract fun get(): T

  /**
   * Returns the contained instance if it is present; `defaultValue` otherwise. If no default
   * value should be required because the instance is known to be present, use [.get]
   * instead. For a default value of `null`, use [.orNull].
   *
   *
   *
   * Note about generics: The signature `T or(T defaultValue)` is overly
   * restrictive. However, the ideal signature, `<S super T> S or(S)`, is not legal
   * Java. As a result, some sensible operations involving subtypes are compile errors:
   * <pre>   `Optional<Integer> optionalInt = getSomeOptionalInt();
   * Number value = optionalInt.or(0.5); // error
   *
   * FluentIterable<? extends Number> numbers = getSomeNumbers();
   * Optional<? extends Number> first = numbers.first();
   * Number value = first.or(0.5); // error`</pre>
   *
   *
   *
   * As a workaround, it is always safe to cast an `Optional<? extends T>` to `Optional<T>`. Casting either of the above example `Optional` instances to `Optional<Number>` (where `Number` is the desired output type) solves the problem:
   * <pre>   `Optional<Number> optionalInt = (Optional) getSomeOptionalInt();
   * Number value = optionalInt.or(0.5); // fine
   *
   * FluentIterable<? extends Number> numbers = getSomeNumbers();
   * Optional<Number> first = (Optional) numbers.first();
   * Number value = first.or(0.5); // fine`</pre>
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this method is similar to Java 8's
   * `Optional.orElse`, but will not accept `null` as a `defaultValue`
   * ([.orNull] must be used instead). As a result, the value returned by this method is
   * guaranteed non-null, which is not the case for the `java.util` equivalent.
   */
  internal abstract fun or(defaultValue: T): T

  /**
   * Returns this `Optional` if it has a value present; `secondChoice` otherwise.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this method has no equivalent in Java 8's
   * `Optional` class; write `thisOptional.isPresent() ? thisOptional : secondChoice`
   * instead.
   */
  internal abstract fun or(secondChoice: Optional<out T>): Optional<T>

  /**
   * Returns the contained instance if it is present; `null` otherwise. If the instance is
   * known to be present, use [.get] instead.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this method is equivalent to Java 8's
   * `Optional.orElse(null)`.
   */
  internal abstract fun orNull(): T?

  /**
   * Returns an immutable singleton [Set] whose only element is the contained instance if it
   * is present; an empty immutable [Set] otherwise.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this method has no equivalent in Java 8's
   * `Optional` class. However, this common usage: <pre>   `<p>
   * for (Foo foo : possibleFoo.asSet()) {
   * doSomethingWith(foo);
   * }`</pre>
   *
   *
   * ... can be replaced with: <pre>   `<p>
   * possibleFoo.ifPresent(foo -> doSomethingWith(foo));`</pre>
   *
   * @since 11.0
   */
  internal abstract fun asSet(): Set<T>

  /**
   * If the instance is present, it is transformed with the given [Function]; otherwise,
   * [Optional.absent] is returned.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this method is similar to Java 8's
   * `Optional.map`, except when `function` returns `null`. In this case this
   * method throws an exception, whereas the Java 8 method returns `Optional.absent()`.
   *
   * @throws NullPointerException if the function returns `null`
   * @since 12.0
   */
  internal abstract fun <V> transform(function: Function<in T, V>): Optional<V>

  /**
   * Returns `true` if `object` is an `Optional` instance, and either the
   * contained references are [equal][Object.equals] to each other or both are absent.
   * Note that `Optional` instances of differing parameterized types can be equal.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** no differences.
   */
  abstract override fun equals(`object`: Any?): Boolean

  /**
   * Returns a hash code for this instance.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this class leaves the specific choice of
   * hash code unspecified, unlike the Java 8 equivalent.
   */
  abstract override fun hashCode(): Int

  /**
   * Returns a string representation for this instance.
   *
   *
   *
   * **Comparison to `java.util.Optional`:** this class leaves the specific string
   * representation unspecified, unlike the Java 8 equivalent.
   */
  abstract override fun toString(): String

  companion object {
    /**
     * Returns an `Optional` instance with no contained reference.
     *
     *
     *
     * **Comparison to `java.util.Optional`:** this method is equivalent to Java 8's
     * `Optional.empty`.
     */
    @JvmStatic fun <T> absent(): Optional<T> {
      return Absent.withType()
    }

    /**
     * Returns an `Optional` instance containing the given non-null reference. To have `null` treated as [.absent], use [.fromNullable] instead.
     *
     *
     *
     * **Comparison to `java.util.Optional`:** no differences.
     *
     * @throws NullPointerException if `reference` is null
     */
    @JvmStatic fun <T> of(reference: T): Optional<T> {
      return Present(checkNotNull(reference))
    }

    /**
     * If `nullableReference` is non-null, returns an `Optional` instance containing that
     * reference; otherwise returns [Optional.absent].
     *
     *
     *
     * **Comparison to `java.util.Optional`:** this method is equivalent to Java 8's
     * `Optional.ofNullable`.
     */
    fun <T> fromNullable(nullableReference: T?): Optional<T> {
      return if (nullableReference == null)
        Optional.absent()
      else
        Present(nullableReference)
    }

    private const val serialVersionUID: Long = 0
  }
}
