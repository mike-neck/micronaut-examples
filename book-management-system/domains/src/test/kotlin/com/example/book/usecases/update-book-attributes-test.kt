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
import com.example.attributes.BookName
import com.example.attributes.Price
import com.example.attributes.PublicationDate
import com.example.book.domains.*
import com.example.book.books.BookId
import com.example.book.repository.BookFinder
import com.example.book.repository.BookWriteRepository
import io.kotlintest.be
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class UpdateBookAttributesTest: BehaviorSpec({

  val bookId = BookId(3000L)
  val bookName = BookName("罪と罰")
  val price = Price(3200)
  val publicationDate = PublicationDate(Instant.now())
  val author = Author(1000L, AuthorName("石田", "三成"))

  given("bookFinder cannot find book") {
    val bookFinder = mockk<BookFinder>()
    every { bookFinder.findById(bookId) } returns null

    val bookWriteRepository = mockk<BookWriteRepository>()

    `when`("invokes use case") {
      val bookUpdate = BookUpdate()
      val updateBookAttributes = UpdateBookAttributes(bookFinder, bookWriteRepository)
      val result = updateBookAttributes(bookId, bookUpdate)

      then("result is Failure") {
        result should instanceOf(Failure::class)
      }

      then("result has cause NOT_FOUND") {
        val failure = result as Failure<Reason, *>
        failure.value.first should be(Cause.NOT_FOUND)
      }
    }
  }

  given("bookFinder finds book") {
    val bookFinder = mockk<BookFinder>()
    every { bookFinder.findById(bookId) } returns
        PublishedBook(bookId, bookName, publicationDate, price, Authors(author))

    and("without update") {
      val bookUpdate = BookUpdate(name = bookName)

      val bookWriteRepository = mockk<BookWriteRepository>()

      `when`("invoke use case") {
        val updateBookAttributes = UpdateBookAttributes(bookFinder, bookWriteRepository)
        val result = updateBookAttributes(bookId, bookUpdate)

        then("result is Failure") {
          result should  instanceOf(Failure::class)
        }
        then("result has cause BAD_PARAM") {
          val failure = result as Failure<Reason, *>
          failure.value.first should be(Cause.BAD_PARAM)
        }
      }
    }

    and("with update") {
      val updatedName = BookName("カラマーゾフの兄弟")
      val updatedPrice = Price(4500)
      val bookUpdate = BookUpdate(name = updatedName, price = updatedPrice)

      and("error in update") {
        val bookWriteRepository = mockk<BookWriteRepository>()
        every { bookWriteRepository.update(any()) } returns null

        `when`("invoke use case") {
          val updateBookAttributes = UpdateBookAttributes(bookFinder, bookWriteRepository)
          val result = updateBookAttributes(bookId, bookUpdate)

          then("result is Failure") {
            result should instanceOf(Failure::class)
          }
          then("result has cause CONFLICT") {
            val failure = result as Failure<Reason, *>
            failure.value.first should be(Cause.CONFLICT)
          }
        }
      }

      and("update succeeds") {
        val bookWriteRepository = mockk<BookWriteRepository>()
        every { bookWriteRepository.update(any()) } returns
            PublishedBook(bookId, updatedName, publicationDate, updatedPrice, Authors(author))

        `when`("invoke use case") {
          val updateBookAttributes = UpdateBookAttributes(bookFinder, bookWriteRepository)
          val result = updateBookAttributes(bookId, bookUpdate)

          then("result is success") {
            result should instanceOf(Success::class)
          }
        }
      }
    }
  }
})
