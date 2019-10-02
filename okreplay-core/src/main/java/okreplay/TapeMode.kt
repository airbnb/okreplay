package okreplay

enum class TapeMode(val isReadable: Boolean, val isWritable: Boolean, val isSequential: Boolean) {
  UNDEFINED(false, false, false), READ_WRITE(true, true, false), READ_ONLY(true, false, false),
  READ_ONLY_QUIET(true, false, false), READ_SEQUENTIAL(true, false, true), WRITE_ONLY(false,
      true, false), WRITE_SEQUENTIAL(false, true, true);

  /**
   * For compatibility with Groovy truth.
   */
  fun asBoolean(): Boolean {
    return isReadable || isWritable
  }

  fun toNullable(): TapeMode? {
    return if (this == UNDEFINED) null else this
  }
}
