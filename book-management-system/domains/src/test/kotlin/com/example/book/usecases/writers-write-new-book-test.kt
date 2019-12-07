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
package com.example.book.usecases

import com.example.book.Failure
import com.example.book.Success
import com.example.book.attributes.BookName
import com.example.book.attributes.Price
import com.example.book.attributes.PublicationDate
import com.example.book.domains.*
import com.example.book.ids.AuthorId
import com.example.book.ids.BookId
import com.example.book.repository.AuthorFinder
import com.example.book.repository.BookWriteRepository
import io.kotlintest.be
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class AuthorsWriteNewBookTest: StringSpec({

  val authorId = AuthorId(1000L)
  val bookId = BookId(3000L)

  val bookName = BookName("Crime and Punishment")
  val publicationDate = PublicationDate(Instant.now())
  val price = Price(3200)

  val manuscript = Manuscript(bookName, publicationDate, price)

  "authorReadRepository#find authorId -> author not found -> Failure" {
    given()
    val authorFinder = mockk<AuthorFinder>()
    every { authorFinder.findById(any()) } returns null

    val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, mockk())

    `when`()
    val result = authorsWritingNewBook(AuthorId(2000L), manuscript)

    then()
    result should instanceOf(Failure::class)
  }

  "bookWriteRepository#save work -> conflict -> Failure" {
    given()
    val authorFinder = mockk<AuthorFinder>()
    every { authorFinder.findById(authorId) } returns Author(authorId, AuthorName("石田", "三成"))

    val bookWriteRepository = mockk<BookWriteRepository>()
    every { bookWriteRepository.save(any()) } returns null

    val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, bookWriteRepository)

    `when`()
    val result = authorsWritingNewBook(authorId, manuscript)

    then()
    result should instanceOf(Failure::class)
  }

  "author found -> book can be saved -> Success" {
    given()
    val authorFinder = mockk<AuthorFinder>()
    val author = Author(authorId, AuthorName("石田", "三成"))
    every { authorFinder.findById(authorId) } returns author

    val bookWriteRepository = mockk<BookWriteRepository>()
    val publishedBook = PublishedBook(bookId, bookName, publicationDate, price, Authors(listOf(author)))
    every { bookWriteRepository.save(any())
    } returns publishedBook

    val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, bookWriteRepository)

    `when`()
    val result = authorsWritingNewBook(authorId, manuscript)

    then()
    result should instanceOf(Success::class)
    result.getOrThrow { cause -> IllegalStateException("${cause.first}/${cause.second}") } should be(publishedBook)
  }
})
