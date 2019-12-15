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

import com.example.util.Failure
import com.example.util.Success
import com.example.attributes.BookName
import com.example.attributes.Price
import com.example.attributes.PublicationDate
import com.example.book.domains.*
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.repository.AuthorFinder
import com.example.book.repository.BookWriteRepository
import io.kotlintest.be
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class AuthorsWriteNewBookTest: BehaviorSpec({

  val authorId = AuthorId(1000L)
  val bookId = BookId(3000L)

  val bookName = BookName("Crime and Punishment")
  val publicationDate = PublicationDate(Instant.now())
  val price = Price(3200)

  val manuscript = Manuscript(bookName, publicationDate, price)

  val author = Author(authorId, AuthorName("石田", "三成"))

  given("author not found") {
    val authorFinder = mockk<AuthorFinder>()
    every { authorFinder.findById(any()) } returns null

    val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, mockk())

    `when`("invoke use case") {
      val result = authorsWritingNewBook(AuthorId(2000L), manuscript)

      then("result should be failure") {
        result should instanceOf(Failure::class)
      }
    }
  }

  given("author found") {
    val authorFinder = mockk<AuthorFinder>()
    every { authorFinder.findById(authorId) } returns author

    and("saving book fails") {
      val bookWriteRepository = mockk<BookWriteRepository>()
      every { bookWriteRepository.save(any()) } returns null

      `when`("invoke use case") {
        val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, bookWriteRepository)

        val result = authorsWritingNewBook(authorId, manuscript)

        then("result should be failure") {
          result should instanceOf(Failure::class)
        }
      }
    }

    and("saving book succeeds") {
      val bookWriteRepository = mockk<BookWriteRepository>()
      val publishedBook = PublishedBook(bookId, bookName, publicationDate, price, Authors(listOf(author)))
      every { bookWriteRepository.save(any()) } returns publishedBook

      `when`("invoke use case") {
        val authorsWritingNewBook = AuthorsWritingNewBook(authorFinder, bookWriteRepository)
        val result = authorsWritingNewBook(authorId, manuscript)

        then("result is success") {
          result should instanceOf(Success::class)
        }
        then("result content is expected publishedBook") {
          val success = result as Success<*, PublishedBook>
          success.value should be(publishedBook)
        }
      }
    }
  }
})
