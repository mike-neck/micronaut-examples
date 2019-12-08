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
package com.example.book.conv

import com.example.book.attributes.BookName
import com.example.book.attributes.Price
import com.example.book.attributes.PublicationDate
import com.example.book.ids.BookId
import org.seasar.doma.ExternalDomain
import org.seasar.doma.jdbc.domain.DomainConverter
import java.util.*

@ExternalDomain
class BookIdConverter: DomainConverter<BookId, Long> {

  override fun fromDomainToValue(domain: BookId?): Long = BookId.toPrimitiveValue(domain)

  override fun fromValueToDomain(value: Long?): BookId = BookId.strict(value)
} 

@ExternalDomain
class BookNameConverter: DomainConverter<BookName, String> {

  override fun fromDomainToValue(domain: BookName?): String =
      domain?.value ?: throw IllegalArgumentException("invalid bookName($domain)")

  override fun fromValueToDomain(value: String?): BookName = BookName.strict(value)
}

@ExternalDomain
class PriceConverter: DomainConverter<Price, Int> {

  override fun fromDomainToValue(domain: Price?): Int =
      domain?.value?: throw IllegalArgumentException("invalid price($domain)")

  override fun fromValueToDomain(value: Int?): Price =
      Price.from(value)?: throw IllegalArgumentException("invalid price value($value)")
}

@ExternalDomain
class PublicationDateConverter: DomainConverter<PublicationDate, Date> {

  override fun fromDomainToValue(domain: PublicationDate?): Date =
      if (domain == null) throw IllegalArgumentException("invalid publicationDate($domain)")
      else Date.from(domain.value)

  override fun fromValueToDomain(value: Date?): PublicationDate =
      if (value == null) throw IllegalArgumentException("invalid publicationDate($value")
      else PublicationDate(value.toInstant())
}
