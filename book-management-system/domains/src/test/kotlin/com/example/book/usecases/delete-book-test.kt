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

import com.example.book.Cause
import com.example.book.Failure
import com.example.book.Reason
import com.example.book.Success
import com.example.book.domains.Authors
import com.example.book.domains.PublishedBook
import com.example.book.repository.BookFinder
import com.example.book.repository.BookWriteRepository
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk

class DeleteBookTest: BehaviorSpec({

  given("book not found") {
    val bookFinder = mockk<BookFinder>()
    every { bookFinder.findById(Values.bookId) } returns null

    val bookWriteRepository = mockk<BookWriteRepository>()

    `when`("invoke use case") {
      val deleteBook = DeleteBook(bookFinder, bookWriteRepository)

      val result = deleteBook(Values.bookId)

      then("result is failure") {
        result should instanceOf(Failure::class)
      }
      then("result cause is NOT_FOUND") {
        val failure = result as Failure<Reason, *>
        failure.value.first shouldBe Cause.NOT_FOUND
      }
    }
  }

  given("book found") {
    val bookFinder = mockk<BookFinder>()
    val publishedBook = PublishedBook(
        Values.bookId, Values.bookName, Values.publicationDate, Values.price, Authors(Values.author))
    every { bookFinder.findById(Values.bookId) } returns publishedBook

    and("deleting book fails") {
      val bookWriteRepository = mockk<BookWriteRepository>()
      every { bookWriteRepository.delete(publishedBook) } returns null

      `when`("invoke use case") {
        val deleteBook = DeleteBook(bookFinder, bookWriteRepository)

        val result = deleteBook(Values.bookId)

        then("result is failure") {
          result should instanceOf(Failure::class)
        }
        then("result cause is CONFLICT") {
          val failure = result as Failure<Reason, *>
          failure.value.first shouldBe Cause.CONFLICT
        }
      }

      and("deleting book finishes in successful") {
        @Suppress("NAME_SHADOWING")
        val bookWriteRepository = mockk<BookWriteRepository>()
        every { bookWriteRepository.delete(publishedBook) } returns Unit

        `when`("invoke use case") {
          val deleteBook = DeleteBook(bookFinder, bookWriteRepository)

          val result = deleteBook(Values.bookId)

          then("result is success") {
            result should instanceOf(Success::class)
          }
          then("result has Unit") {
            val any = result as Success<*, *>
            any.value shouldBe Unit
          }
        }
      }
    }
  }
})
