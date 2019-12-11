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
import com.example.book.ids.BookId
import com.example.book.usecases.AuthorsWritingNewBook
import com.example.book.usecases.DeleteBook
import com.example.book.usecases.FindBooks
import com.example.book.usecases.UpdateBookAttributes
import com.example.http.HttpError
import com.example.http.toHttpError
import com.example.http.toResponse
import com.example.http.validationErrorToHttpError
import com.example.json.BookJson
import com.example.util.nullToValidationError
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Produces
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
  fun getBooks(@PathVariable id: String?): HttpResponse<*> =
      BookId.fromString(id).nullToValidationError { listOf("not found book($id)") }
          .mapFailure { validationError -> HttpStatus.NOT_FOUND to validationError }
          .flatMap { bookQueryService.findBookById(it).mapFailure { it.toHttpError() } }
          .run(onFailure = { logger.info("getBooks: failure, book: {}, status: {}, error: {}", id, it.first, it.second) })
          .map { HttpResponse.ok(it) as HttpResponse<*> }
          .rescue { it.toResponse() }
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
    private val authorsWritingNewBook: AuthorsWritingNewBook,
    private val updateBookAttributes: UpdateBookAttributes,
    private val deleteBook: DeleteBook
)
