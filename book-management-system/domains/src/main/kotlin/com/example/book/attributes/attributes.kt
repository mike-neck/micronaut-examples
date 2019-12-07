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
package com.example.book.attributes

import java.time.DateTimeException
import java.time.Instant

interface StringAttributeFactory<T: Any> {
  fun from(value: String?): T? = when {
    value == null -> null
    value.isEmpty() -> null
    value.isBlank() -> null
    value.codePointCount(0, value.length) > maxLength -> null
    else -> const(value)
  }
  val maxLength: Int
  fun const(value: String): T
}

data class BookName(val value: String) {
  companion object: StringAttributeFactory<BookName> {
    override val maxLength: Int = 120
    override fun const(value: String): BookName = BookName(value)
  }
}

data class Price(val value: Int) {
  companion object {
    fun from(value: Int?): Price? =
        when {
          value == null -> null
          value < 0 -> null
          else -> Price(value)
        }
  }
}

data class AuthorFirstName(val value: String) {
  companion object: StringAttributeFactory<AuthorFirstName> {
    override val maxLength: Int = 50
    override fun const(value: String): AuthorFirstName = AuthorFirstName(value)
  }
}

data class AuthorLastName(val value: String) {
  companion object: StringAttributeFactory<AuthorLastName> {
    override val maxLength: Int = 50
    override fun const(value: String): AuthorLastName = AuthorLastName(value)
  }
}

data class PublicationDate(val value: Instant) {
  companion object {
    fun from(value: String?): PublicationDate? =
        if (value == null) null
        else try {
          val date = Instant.parse(value)
          PublicationDate(date)
        } catch (e: DateTimeException) {
          null
        }
  }
}
