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
import org.seasar.doma.Id
import org.seasar.doma.Table
import java.time.Instant

@Entity(immutable = true)
@Table(name = "books")
data class BookTable(
    @Id val id: BookId,
    val name: BookName,
    val price: Price,
    val publicationDate: PublicationDate
) {
  @TestOnly
  constructor(id: Long, name: String, price: Int, date: Instant):
      this(BookId(id), BookName(name), Price(price), PublicationDate(date))
}

@Entity(immutable = true)
@Table(name = "writings")
data class Writing(
    @Id val bookId: BookId,
    @Id val authorId: AuthorId
)

@Entity(immutable = true)
@Table(name = "authors")
data class AuthorTable(
    @Id val id: AuthorId,
    val firstName: AuthorFirstName,
    val lastName: AuthorLastName
)
