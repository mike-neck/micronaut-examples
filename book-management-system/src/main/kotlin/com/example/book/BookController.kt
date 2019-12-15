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
package com.example.book

import com.example.Logger
import com.example.attributes.BookName
import com.example.attributes.Price
import com.example.attributes.PublicationDate
import com.example.book.domains.BookUpdate
import com.example.book.books.BookId
import com.example.book.usecases.DeleteBook
import com.example.book.usecases.FindBooks
import com.example.book.usecases.UpdateBookAttributes
import com.example.http.nullToHttpError
import com.example.http.toHttpError
import com.example.http.toResponse
import com.example.json.BookJson
import com.example.util.Reason
import com.example.util.ResultEx
import com.example.util.nullToValidationError
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import javax.inject.Inject
import javax.inject.Singleton

@Controller("/books")
class BookController
@Inject constructor(
    private val bookQueryService: BookQueryService,
    private val bookCommandService: BookCommandService
) {

  private val logger: Logger<BookController> = Logger.get()

  @Get("{id}")
  @Produces("application/json")
  fun getBook(@PathVariable id: String?): HttpResponse<*> =
      BookId.fromString(id).nullToValidationError { listOf("not found book($id)") }
          .mapFailure { validationError -> HttpStatus.NOT_FOUND to validationError }
          .flatMap { bookQueryService.findBookById(it).mapFailure { reason -> reason.toHttpError() } }
          .run(onFailure = { logger.info("getBook: failure, book: {}, status: {}, error: {}", id, it.first, it.second) })
          .map { HttpResponse.ok(it) as HttpResponse<*> }
          .rescue { it.toResponse() }

  @Patch("{id}")
  @Consumes("application/json", "application/x-www-form-urlencoded")
  @Produces("application/json")
  fun updateBook(
      @PathVariable("id") id: String?,
      @Body("name") name: String?,
      @Body("price") price: String?,
      @Body("publish") publish: String?
  ): HttpResponse<*> =
      BookId.fromString(id).nullToHttpError { HttpStatus.NOT_FOUND to listOf("not found book($id)") }
          .map { it to bookUpdate(name, price, publish) }
          .flatMap { pair -> bookCommandService.updateBook(pair.first, pair.second).mapFailure { reason -> reason.toHttpError() } }
          .run(
              onSuccess = { logger.info("updateBook: success, book: {}", it.id) },
              onFailure = { logger.info("updateBook: failure, book: {}, status: {}, error: {}", id, it.first, it.second) }
          ).map { HttpResponse.ok(it) as HttpResponse<*> }
          .rescue { it.toResponse() }

  @Delete("{id}")
  fun deleteBook(@PathVariable("id") id: String?): HttpResponse<*> =
      BookId.fromString(id).nullToHttpError { HttpStatus.NOT_FOUND to listOf("not found book($id)") }
          .flatMap { bookId -> bookCommandService.deleteBookById(bookId).mapFailure { it.toHttpError() } }
          .run(
              onSuccess = { logger.info("deleteBook: success, book: {}", id) },
              onFailure = { logger.info("deleteBook: failure, book: {}, status: {}, error: {}", id, it.first, it.second) }
          ).map { HttpResponse.noContent<Unit>() as HttpResponse<*> }
          .rescue { it.toResponse() }

  companion object {
    fun bookUpdate(name: String?, price: String?, publish: String?): BookUpdate =
        BookUpdate(BookName.from(name), PublicationDate.from(publish), Price.from(price))
  }
}

@Singleton
class BookQueryService
@Inject constructor(
    private val findBooks: FindBooks
) {
  fun findBookById(bookId: BookId): ResultEx<Reason, BookJson> =
      findBooks(bookId).map { book -> BookJson(book) }
}

@Singleton
class BookCommandService
@Inject constructor(
    private val updateBookAttributes: UpdateBookAttributes,
    private val deleteBook: DeleteBook
) {
  fun updateBook(bookId: BookId, bookUpdate: BookUpdate): ResultEx<Reason, BookJson> =
      updateBookAttributes(bookId, bookUpdate).map { book -> BookJson(book) }

  fun deleteBookById(bookId: BookId): ResultEx<Reason, Unit> = deleteBook(bookId)
}
