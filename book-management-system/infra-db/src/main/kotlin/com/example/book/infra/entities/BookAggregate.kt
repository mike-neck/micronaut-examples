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
package com.example.book.infra.entities

import com.example.book.attributes.*
import com.example.book.ids.AuthorId
import com.example.book.ids.BookId
import org.jetbrains.annotations.TestOnly
import org.seasar.doma.Entity
import java.time.Instant

@Entity(immutable = true)
data class BookAggregate(
    val bookId: BookId,
    val name: BookName,
    val price: Price,
    val publicationDate: PublicationDate,
    val authorId: AuthorId,
    val firstName: AuthorFirstName,
    val lastName: AuthorLastName
) {
  @TestOnly
  constructor(
      bookId: Long,
      name: String,
      price: Int,
      date: Instant,
      authorId: Long,
      firstName: String,
      lastName: String):
      this(
          BookId(bookId),
          BookName(name),
          Price(price),
          PublicationDate(date),
          AuthorId(authorId),
          AuthorFirstName(firstName),
          AuthorLastName(lastName)
      )
}
