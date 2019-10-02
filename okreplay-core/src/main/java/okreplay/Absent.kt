package okreplay

import okreplay.Util.checkNotNull
import java.util.function.Function

internal class Absent<T> private constructor() : Optional<T>() {

  override val isPresent: Boolean = false

  override fun get(): T {
    throw IllegalStateException("Optional.get() cannot be called on an absent value")
  }

  override fun or(defaultValue: T): T {
    return checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)")
  }

  @Suppress("UNCHECKED_CAST") // safe covariant cast
  override fun or(secondChoice: Optional<out T>): Optional<T> {
    return checkNotNull(secondChoice) as Optional<T>
  }

  override fun orNull(): T? {
    return null
  }

  override fun asSet(): Set<T> {
    return emptySet()
  }

  override fun <V> transform(function: Function<in T, V>): Optional<V> {
    checkNotNull(function)
    return Optional.absent()
  }

  override fun equals(`object`: Any?): Boolean {
    return `object` === this
  }

  override fun hashCode(): Int {
    return 0x79a31aac
  }

  override fun toString(): String {
    return "Optional.absent()"
  }

  private fun readResolve(): Any {
    return INSTANCE
  }

  companion object {
    private val INSTANCE = Absent<Any>()

    @Suppress("UNCHECKED_CAST") // implementation is "fully variant"
    fun <T> withType(): Optional<T> {
      return INSTANCE as Optional<T>
    }

    private val serialVersionUID: Long = 0
  }
}