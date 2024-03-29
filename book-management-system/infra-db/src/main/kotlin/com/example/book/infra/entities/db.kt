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

import com.example.util.Change
import com.example.attributes.*
import com.example.book.domains.BookChange
import com.example.book.domains.Work
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.books.BookId.Companion.newBookId
import com.example.ids.IdGen
import org.jetbrains.annotations.TestOnly
import org.seasar.doma.Entity
import org.seasar.doma.Id
import org.seasar.doma.Table
import java.time.Instant

@Entity(immutable = true)
@Table(name = "books")
data class BookRecord(
    @Id val id: BookId,
    val name: BookName,
    val price: Price,
    val publicationDate: PublicationDate
) {
  @TestOnly
  constructor(id: Long, name: String, price: Int, date: Instant):
      this(BookId(id), BookName(name), Price(price), PublicationDate(date))

  fun applyChange(bookChange: BookChange): BookRecord =
      BookRecord(
          id,
          name.accept(bookChange.name),
          price.accept(bookChange.price),
          publicationDate.accept(bookChange.publicationDate)
      )

  private fun <T> T.accept(change: Change<T>): T = change.apply(this)

  companion object {
    fun fromWork(idGen: IdGen, work: Work): BookRecord =
        work.book.let { BookRecord(idGen.newBookId(), it.name, it.price, it.publicationDate) }
  }
}

@Entity(immutable = true)
@Table(name = "writings")
data class WritingRecord(
    @Id val bookId: BookId,
    @Id val authorId: AuthorId
)

@Entity(immutable = true)
@Table(name = "authors")
data class AuthorRecord(
    @Id val id: AuthorId,
    val firstName: AuthorFirstName,
    val lastName: AuthorLastName
)
