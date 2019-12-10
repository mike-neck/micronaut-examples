/*
 * Copyright 2019 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.book.ids

import java.lang.NumberFormatException

interface IdGen {
  fun newLongId(): Long
}

interface Id<T> {
  val value: T
}

interface StrictId<T: Id<V>, V> {
  fun strict(value: V?): T =
      if (value != null) from(value)
      else throw IllegalArgumentException("invalid $nameOfId")

  fun from(value: V): T

  fun fromString(value: String?): T? =
      stringToValue(value)?.let { from(it) }

  fun stringToValue(value: String?): V?

  val nameOfId: String
  fun toPrimitiveValue(value: T?): V =
      if (value == null) throw IllegalArgumentException("invalid $nameOfId")
      else value.value
}

private fun String?.toLongOrNull(): Long? = try {
  this?.toLong()
} catch (e: NumberFormatException) {
  null
}

data class BookId(override val value: Long): Id<Long> {
  companion object : StrictId<BookId, Long> {
    override fun from(value: Long): BookId = BookId(value)
    override fun stringToValue(value: String?): Long? = value.toLongOrNull()
    override val nameOfId: String = "bookId"
    fun IdGen.newBookId(): BookId = BookId(this.newLongId())
  }
}

data class AuthorId(override val value: Long): Id<Long> {
  companion object: StrictId<AuthorId, Long> {
    override fun from(value: Long): AuthorId = AuthorId(value)
    override fun stringToValue(value: String?): Long? = value.toLongOrNull()
    override val nameOfId: String = "authorId"
    fun IdGen.newAuthorId(): AuthorId = AuthorId(this.newLongId())
  }
}
