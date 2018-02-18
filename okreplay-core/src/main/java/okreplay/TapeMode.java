package okreplay;

public enum TapeMode {
  UNDEFINED(false, false, false), READ_WRITE(true, true, false), READ_ONLY(true, false, false),
  READ_ONLY_QUIET(true, false, false), READ_SEQUENTIAL(true, false, true), WRITE_ONLY(false,
      true, false), WRITE_SEQUENTIAL(false, true, true);

  private final boolean readable;
  private final boolean writable;
  private final boolean sequential;

  TapeMode(boolean readable, boolean writable, boolean sequential) {
    this.readable = readable;
    this.writable = writable;
    this.sequential = sequential;
  }

  public boolean isReadable() {
    return readable;
  }

  public boolean isWritable() {
    return writable;
  }

  public boolean isSequential() {
    return sequential;
  }

  /**
   * For compatibility with Groovy truth.
   */
  public boolean asBoolean() {
    return readable || writable;
  }

  public Optional<TapeMode> toOptional() {
    if (this.equals(TapeMode.UNDEFINED)) {
      return Optional.absent();
    } else {
      return Optional.of(this);
    }
  }
}
