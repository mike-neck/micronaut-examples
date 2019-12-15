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

import com.example.util.Cause
import com.example.util.Failure
import com.example.util.Reason
import com.example.util.Success
import com.example.book.domains.Authors
import com.example.book.domains.PublishedBook
import com.example.book.books.BookId
import com.example.book.repository.BookFinder
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk

class FindBooksTest: BehaviorSpec({

  given("bookFinder cannot find a book from given bookId") {
    val bookFinder = bookFinders.cannotFindBook()

    `when`("invoke use case") {
      val findBooks = FindBooks(bookFinder)
      val result = findBooks(bookId)

      then("result is failure") {
        result should instanceOf(Failure::class)
      }
      then("result's cause is NOT_FOUND") {
        val failure = result as Failure<Reason, *>
        failure.value.first shouldBe Cause.NOT_FOUND
      }
    }
  }

  given("bookFinder can find a book from given bookId") {
    val publishedBook = PublishedBook(bookId, bookName, publicationDate, price, Authors(author))
    val bookFinder = bookFinders.canFindBook(bookId to publishedBook)

    `when`("invoke use case") {
      val findBooks = FindBooks(bookFinder)
      val result = findBooks(bookId)

      then("result is success") {
        result should instanceOf(Success::class)
      }
      then("result has expected publishedBook") {
        val success = result as Success<*, PublishedBook>
        success.value shouldBe publishedBook
      }
    }
  }
}) {
  companion object: ValueFactory {
    val bookFinders: BookFinderFactory = object : BookFinderFactory {}
  }
}

interface BookFinderFactory {
  fun cannotFindBook(): BookFinder =
      mockk<BookFinder>().also { every { it.findById(any()) } returns null }

  fun canFindBook(pair: Pair<BookId, PublishedBook>): BookFinder =
      mockk<BookFinder>().also { every { it.findById(pair.first) } returns pair.second }
}
