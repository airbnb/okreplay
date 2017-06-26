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

import okreplay.Util.checkNotNull

enum class CaseFormat constructor(
    private val wordBoundary: CharMatcher,
    private val wordSeparator: String) {
  /** Hyphenated variable naming convention, e.g., "lower-hyphen". */
  LOWER_HYPHEN(CharMatcher.`is`('-'), "-") {
    override fun normalizeWord(word: String): String {
      return Ascii.toLowerCase(word)
    }

    override fun convert(format: CaseFormat, s: String): String {
      if (format === LOWER_UNDERSCORE) {
        return s.replace('-', '_')
      }
      if (format === UPPER_UNDERSCORE) {
        return Ascii.toUpperCase(s.replace('-', '_'))
      }
      return super.convert(format, s)
    }
  },

  /** C++ variable naming convention, e.g., "lower_underscore". */
  LOWER_UNDERSCORE(CharMatcher.`is`('_'), "_") {
    override fun normalizeWord(word: String): String {
      return Ascii.toLowerCase(word)
    }

    override fun convert(format: CaseFormat, s: String): String {
      if (format === LOWER_HYPHEN) {
        return s.replace('_', '-')
      }
      if (format === UPPER_UNDERSCORE) {
        return Ascii.toUpperCase(s)
      }
      return super.convert(format, s)
    }
  },

  /** Java variable naming convention, e.g., "lowerCamel". */
  LOWER_CAMEL(CharMatcher.inRange('A', 'Z'), "") {
    override fun normalizeWord(word: String): String {
      return firstCharOnlyToUpper(word)
    }
  },

  /** Java and C++ class naming convention, e.g., "UpperCamel". */
  UPPER_CAMEL(CharMatcher.inRange('A', 'Z'), "") {
    override fun normalizeWord(word: String): String {
      return firstCharOnlyToUpper(word)
    }
  },

  /** Java and C++ constant naming convention, e.g., "UPPER_UNDERSCORE". */
  UPPER_UNDERSCORE(CharMatcher.`is`('_'), "_") {
    override fun normalizeWord(word: String): String {
      return Ascii.toUpperCase(word)
    }

    override fun convert(format: CaseFormat, s: String): String {
      if (format === LOWER_HYPHEN) {
        return Ascii.toLowerCase(s.replace('_', '-'))
      }
      if (format === LOWER_UNDERSCORE) {
        return Ascii.toLowerCase(s)
      }
      return super.convert(format, s)
    }
  };

  /**
   * Converts the specified `String str` from this format to the specified `format`. A
   * "best effort" approach is taken; if `str` does not conform to the assumed format, then
   * the behavior of this method is undefined but we make a reasonable effort at converting anyway.
   */
  fun to(format: CaseFormat, str: String): String {
    checkNotNull(format)
    checkNotNull(str)
    return if (format === this) str else convert(format, str)
  }

  /** Enum values can override for performance reasons. */
  internal open fun convert(format: CaseFormat, s: String): String {
    // deal with camel conversion
    var out: StringBuilder? = null
    var i = 0
    var j = -1
    j = wordBoundary.indexIn(s, ++j)
    while (j != -1) {
      if (i == 0) {
        // include some extra space for separators
        out = StringBuilder(s.length + 4 * wordSeparator.length)
        out.append(format.normalizeFirstWord(s.substring(i, j)))
      } else {
        out!!.append(format.normalizeWord(s.substring(i, j)))
      }
      out.append(format.wordSeparator)
      i = j + wordSeparator.length
      j = wordBoundary.indexIn(s, ++j)
    }
    return if (i == 0)
      format.normalizeFirstWord(s)
    else
      out!!.append(format.normalizeWord(s.substring(i))).toString()
  }

  internal abstract fun normalizeWord(word: String): String

  private fun normalizeFirstWord(word: String): String {
    return if (this === LOWER_CAMEL) Ascii.toLowerCase(word) else normalizeWord(word)
  }

  fun firstCharOnlyToUpper(word: String): String {
    return if (word.isEmpty())
      word
    else
      Ascii.toUpperCase(word[0]) + Ascii.toLowerCase(word.substring(1))
  }
}
