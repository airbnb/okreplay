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

internal object Ascii {
  /**
   * Returns a copy of the input string in which all [uppercase ASCII][.isUpperCase] have been
   * converted to lowercase. All other characters are copied without modification.
   */
  fun toLowerCase(string: String): String {
    val length = string.length
    var i = 0
    while (i < length) {
      if (isUpperCase(string[i])) {
        val chars = string.toCharArray()
        while (i < length) {
          val c = chars[i]
          if (isUpperCase(c)) {
            chars[i] = (c.toInt() xor 0x20).toChar()
          }
          i++
        }
        return String(chars)
      }
      i++
    }
    return string
  }

  /**
   * Returns a copy of the input string in which all [lowercase ASCII][.isLowerCase] have been
   * converted to uppercase. All other characters are copied without modification.
   */
  fun toUpperCase(string: String): String {
    val length = string.length
    var i = 0
    while (i < length) {
      if (isLowerCase(string[i])) {
        val chars = string.toCharArray()
        while (i < length) {
          val c = chars[i]
          if (isLowerCase(c)) {
            chars[i] = (c.toInt() and 0x5f).toChar()
          }
          i++
        }
        return String(chars)
      }
      i++
    }
    return string
  }

  /**
   * If the argument is a [lowercase ASCII character][.isLowerCase] returns the
   * uppercase equivalent. Otherwise returns the argument.
   */
  fun toUpperCase(c: Char): Char {
    return if (isLowerCase(c)) (c.toInt() and 0x5f).toChar() else c
  }

  /**
   * Indicates whether `c` is one of the twenty-six lowercase ASCII alphabetic characters
   * between `'a'` and `'z'` inclusive. All others (including non-ASCII characters)
   * return `false`.
   */
  private fun isLowerCase(c: Char): Boolean {
    // Note: This was benchmarked against the alternate expression "(char)(c - 'a') < 26" (Nov '13)
    // and found to perform at least as well, or better.
    return c in 'a'..'z'
  }

  /**
   * Indicates whether `c` is one of the twenty-six uppercase ASCII alphabetic characters
   * between `'A'` and `'Z'` inclusive. All others (including non-ASCII characters)
   * return `false`.
   */
  private fun isUpperCase(c: Char): Boolean {
    return c in 'A'..'Z'
  }
}
