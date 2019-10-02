package okreplay

import java.util.Collections
import java.util.function.Function
import java.util.function.Supplier

import okreplay.Util.checkNotNull

internal class Present<T>(private val reference: T) : Optional<T>() {

  override val isPresent: Boolean = true

  override fun get(): T {
    return reference
  }

  override fun or(defaultValue: T): T {
    checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)")
    return reference
  }

  override fun or(secondChoice: Optional<out T>): Optional<T> {
    checkNotNull(secondChoice)
    return this
  }

  override fun orNull(): T? {
    return reference
  }

  override fun asSet(): Set<T> {
    return setOf(reference)
  }

  override fun <V> transform(function: Function<in T, V>): Optional<V> {
    return Present(
        checkNotNull(
            function.apply(reference),
            "the Function passed to Optional.transform() must not return null."))
  }

  override fun equals(`object`: Any?): Boolean {
    if (`object` is Present<*>) {
      val other = `object` as Present<*>?
      return reference == other!!.reference
    }
    return false
  }

  override fun hashCode(): Int {
    return 0x598df91c + reference.hashCode()
  }

  override fun toString(): String {
    return "Optional.of($reference)"
  }

  companion object {

    private val serialVersionUID: Long = 0
  }
}
