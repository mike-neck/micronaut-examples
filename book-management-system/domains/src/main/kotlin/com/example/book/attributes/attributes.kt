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

interface StringAttributeFactory<T: StringBasedValue> {
  fun from(value: String?): T? = when {
    value == null -> null
    value.isEmpty() -> null
    value.isBlank() -> null
    value.codePointCount(0, value.length) > maxLength -> null
    else -> const(value)
  }
  val maxLength: Int
  fun const(value: String): T
  val attributeName: String
  fun strict(value: String?): T = from(value)?: throw IllegalArgumentException("invalid value")

  fun toPrimitiveValue(value: T?): String =
      value?.value ?: throw IllegalArgumentException("invalid $attributeName")
}

interface StringBasedValue {
  val value: String
}

data class BookName(override val value: String): StringBasedValue {
  companion object: StringAttributeFactory<BookName> {
    override val maxLength: Int = 120
    override fun const(value: String): BookName = BookName(value)
    override val attributeName: String = "bookName"
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

data class AuthorFirstName(override val value: String): StringBasedValue {
  companion object: StringAttributeFactory<AuthorFirstName> {
    override val maxLength: Int = 50
    override fun const(value: String): AuthorFirstName = AuthorFirstName(value)
    override val attributeName: String = "authorFirstName"
  }
}

data class AuthorLastName(override val value: String): StringBasedValue {
  companion object: StringAttributeFactory<AuthorLastName> {
    override val maxLength: Int = 50
    override fun const(value: String): AuthorLastName = AuthorLastName(value)
    override val attributeName: String = "authorLastName"
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
