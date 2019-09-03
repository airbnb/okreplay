/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package okreplay

import java.util.BitSet

import okreplay.Util.checkArgument
import okreplay.Util.checkNotNull
import okreplay.Util.checkPositionIndex

// Constructors

/**
 * Constructor for use by subclasses. When subclassing, you may want to override
 * `toString()` to provide a useful description.
 */
internal abstract class CharMatcher : Predicate<Char> {

  // Abstract methods

  /** Determines a true or false value for the given character.  */
  abstract fun matches(c: Char): Boolean

  // Non-static factories

  /** Returns a matcher that matches any character matched by both this matcher and `other`. */
  open fun and(other: CharMatcher): CharMatcher {
    return And(this, other)
  }

  /** Returns a matcher that matches any character matched by either this matcher or `other`. */
  open fun or(other: CharMatcher): CharMatcher {
    return Or(this, other)
  }

  /** Sets bits in `table` matched by this matcher. */
  internal open fun setBits(table: BitSet) {
    var c = Character.MAX_VALUE.toInt()
    while (c >= Character.MIN_VALUE.toInt()) {
      if (matches(c.toChar())) {
        table.set(c)
      }
      c--
    }
  }

  // Text processing routines

  /**
   * Returns the index of the first matching character in a character sequence, starting from a
   * given position, or `-1` if no character matches after that position.

   *
   * The default implementation iterates over the sequence in forward order, beginning at `start`,
   * calling [.matches] for each character.

   * @param sequence the character sequence to examine
   * *
   * @param start the first index to examine; must be nonnegative and no greater than `sequence.length()`
   * *
   * @return the index of the first matching character, guaranteed to be no less than `start`,
   * *     or `-1` if no character matches
   * *
   * @throws IndexOutOfBoundsException if start is negative or greater than `sequence.length()`
   */
  open fun indexIn(sequence: CharSequence, start: Int): Int {
    val length = sequence.length
    checkPositionIndex(start, length)
    return (start..length - 1).firstOrNull { matches(sequence[it]) } ?: -1
  }

  @Deprecated("Provided only to satisfy the Predicate interface; use matches instead.")
  override fun apply(character: Char?): Boolean {
    return matches(character!!)
  }

  // Fast matchers

  /** A matcher for which precomputation will not yield any significant benefit.  */
  internal abstract class FastMatcher : CharMatcher()

  /** [FastMatcher] which overrides `toString()` with a custom name.  */
  internal abstract class NamedFastMatcher(description: String) : FastMatcher() {
    private val description: String = checkNotNull(description)

    override fun toString(): String {
      return description
    }
  }

  // Static constant implementation classes

  /** Implementation of [.none].  */
  private class None private constructor() : NamedFastMatcher("CharMatcher.none()") {
    override fun matches(c: Char): Boolean {
      return false
    }

    override fun indexIn(sequence: CharSequence, start: Int): Int {
      val length = sequence.length
      checkPositionIndex(start, length)
      return -1
    }

    override fun and(other: CharMatcher): CharMatcher {
      checkNotNull(other)
      return this
    }

    override fun or(other: CharMatcher): CharMatcher {
      return checkNotNull(other)
    }

    companion object {

      internal val INSTANCE = None()
    }

  }

  // Non-static factory implementation classes

  /** Implementation of [.and].  */
  private class And internal constructor(
      a: CharMatcher, b: CharMatcher,
      internal val first: CharMatcher = checkNotNull(a)) : CharMatcher() {

    internal val second: CharMatcher = checkNotNull(b)

    override fun matches(c: Char): Boolean {
      return first.matches(c) && second.matches(c)
    }

    override fun setBits(table: BitSet) {
      val tmp1 = BitSet()
      first.setBits(tmp1)
      val tmp2 = BitSet()
      second.setBits(tmp2)
      tmp1.and(tmp2)
      table.or(tmp1)
    }

    override fun toString(): String {
      return "CharMatcher.and($first, $second)"
    }
  }

  /** Implementation of [.or].  */
  private class Or internal constructor(a: CharMatcher, b: CharMatcher) : CharMatcher() {
    internal val first: CharMatcher = checkNotNull(a)
    internal val second: CharMatcher = checkNotNull(b)

    override fun setBits(table: BitSet) {
      first.setBits(table)
      second.setBits(table)
    }

    override fun matches(c: Char): Boolean {
      return first.matches(c) || second.matches(c)
    }

    override fun toString(): String {
      return "CharMatcher.or($first, $second)"
    }
  }

  // Static factory implementations

  /** Implementation of [.is].  */
  private class Is internal constructor(private val match: Char) : FastMatcher() {

    override fun matches(c: Char): Boolean {
      return c == match
    }

    override fun and(other: CharMatcher): CharMatcher {
      return if (other.matches(match)) this else none()
    }

    override fun or(other: CharMatcher): CharMatcher {
      return if (other.matches(match)) other else super.or(other)
    }

    override fun setBits(table: BitSet) {
      table.set(match.toInt())
    }

    override fun toString(): String {
      return "CharMatcher.is('" + showCharacter(match) + "')"
    }
  }

  /** Implementation of [.inRange].  */
  private class InRange internal constructor(
      private val startInclusive: Char,
      private val endInclusive: Char) : FastMatcher() {

    init {
      checkArgument(endInclusive >= startInclusive)
    }

    override fun matches(c: Char): Boolean {
      return c in startInclusive..endInclusive
    }

    override fun setBits(table: BitSet) {
      table.set(startInclusive.toInt(), endInclusive.toInt() + 1)
    }

    override fun toString(): String {
      return "CharMatcher.inRange('${showCharacter(startInclusive)}', '${showCharacter(endInclusive)}')"
    }
  }

  companion object {
    // Constant matcher factory methods

    /**
     * Matches no characters.

     * @since 19.0 (since 1.0 as constant `NONE`)
     */
    fun none(): CharMatcher {
      return None.INSTANCE
    }

    // Static factories

    /**
     * Returns a `char` matcher that matches only one specified character.
     */
    fun `is`(match: Char): CharMatcher {
      return Is(match)
    }

    /**
     * Returns a `char` matcher that matches any character in a given range (both endpoints are
     * inclusive). For example, to match any lowercase letter of the English alphabet, use
     * `CharMatcher.inRange('a', 'z')`.
     * @throws IllegalArgumentException if `endInclusive < startInclusive`
     */
    fun inRange(startInclusive: Char, endInclusive: Char): CharMatcher {
      return InRange(startInclusive, endInclusive)
    }

    /**
     * Returns the Java Unicode escape sequence for the given character, in the form "\u12AB" where
     * "12AB" is the four hexadecimal digits representing the 16 bits of the UTF-16 character.
     */
    private fun showCharacter(c: Char): String {
      var c = c
      val hex = "0123456789ABCDEF"
      val tmp = charArrayOf('\\', 'u', '\u0000', '\u0000', '\u0000', '\u0000')
      for (i in 0..3) {
        tmp[5 - i] = hex[c.toInt() and 0xF]
        c = (c.toInt() shr 4).toChar()
      }
      return String(tmp)
    }
  }
}
