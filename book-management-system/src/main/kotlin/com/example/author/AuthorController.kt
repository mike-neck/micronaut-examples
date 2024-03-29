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
package com.example.author

import com.example.Logger
import com.example.util.ResultEx
import com.example.attributes.*
import com.example.book.domains.AuthorName
import com.example.book.domains.Manuscript
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.usecases.AuthorsWritingNewBook
import com.example.book.usecases.CreateNewAuthor
import com.example.book.usecases.FindAuthors
import com.example.book.usecases.FindBooks
import com.example.http.*
import com.example.json.AuthorJson
import com.example.json.BookJson
import com.example.util.nullToValidationError
import com.example.util.zipWith
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import java.net.URI
import javax.inject.Inject

@Controller("/authors")
class AuthorController
@Inject constructor(
    private val createNewAuthor: CreateNewAuthor,
    private val writingNewBook: AuthorsWritingNewBook,
    private val findBooks: FindBooks,
    private val findAuthors: FindAuthors
) {

  private val logger: Logger<AuthorController> = Logger.get()

  private fun CreateNewAuthor.apply(authorName: AuthorName): ResultEx<HttpError, AuthorJson> =
      this(authorName)
          .map { author -> AuthorJson(author) }
          .mapFailure { it.toHttpError() }

  @Get("{id}")
  @Produces("application/json")
  fun getAuthor(@PathVariable("id") id: String?): HttpResponse<*> =
      AuthorId.fromString(id)
          .nullToValidationError { listOf("invalid number format(id:$id)") }
          .validationErrorToHttpError
          .flatMap { findAuthors(it).nullToHttpError { HttpStatus.NOT_FOUND to listOf("author not found(id:$id)") } }
          .run(
              onFailure = { logger.info("getAuthor: failure, id: {}, error: {}", id, it.second) })
          .map { HttpResponse.ok(AuthorJson(it)) as HttpResponse<*> }
          .rescue { it.toResponse() }

  @Post
  @Consumes("application/json", "application/x-www-form-urlencoded")
  @Produces("application/json")
  fun createNewAuthor(
      @Body("firstName") firstName: String?,
      @Body("lastName") lastName: String?): HttpResponse<*> =
      AuthorFirstName.from(firstName).nullToValidationError { listOf("invalid firstName") }
          .zipWith { AuthorLastName.from(lastName).nullToValidationError { listOf("invalid lastName") } }
          .validationErrorToHttpError
          .flatMap { createNewAuthor.apply(AuthorName.fromPair(it)) }
          .run(
              onSuccess = { logger.info("createNewAuthor: success, author: {}", it) },
              onFailure = { logger.info("createNewAuthor: failure, firstName: {}, lastName: {}, error: {}", firstName, lastName, it.second) })
          .map { HttpResponse.created<AuthorJson>(URI.create("/authors/${it.id}")).body(it) as HttpResponse<*> }
          .rescue { it.toResponse() }

  private fun FindBooks.apply(pair: Pair<AuthorId, BookId>): ResultEx<HttpError, BookJson> =
      this(pair.first, pair.second)
          .map { publishedBook -> BookJson(publishedBook) }
          .mapFailure { it.toHttpError() }

  @Get("{id}/books/{bookId}")
  @Produces("application/json")
  fun getAuthorsBook(
      @PathVariable("id") author: String,
      @PathVariable("bookId") book: String): HttpResponse<*> =
      AuthorId.fromString(author).nullToValidationError { listOf("not found(author:$author,book:$book)") }
          .zipWith { BookId.fromString(book).nullToValidationError { listOf("not found(author:$author,book:$book)") } }
          .validationErrorToHttpError
          .mapFailure { pair -> HttpStatus.NOT_FOUND to pair.second }
          .flatMap { findBooks.apply(it) }
          .run(
              onFailure = { logger.info("getAuthorsBook: failure, author: {}, book: {}", author, book) })
          .map { bookJson -> HttpResponse.ok(bookJson) as HttpResponse<*> }
          .rescue { it.toResponse() }
  

  private fun AuthorsWritingNewBook.apply(pair: Pair<AuthorId, Manuscript>): ResultEx<HttpError, BookJson> =
      this(pair.first, pair.second)
          .map { publishedBook -> BookJson(publishedBook) }
          .mapFailure { it.toHttpError() }

  @Post("{id}/books")
  @Consumes("application/json", "application/x-www-form-urlencoded")
  @Produces("application/json")
  fun writeBook(
      @PathVariable("id") id: String?,
      @Body("name") name: String?,
      @Body("price") price: String?,
      @Body("publish") publish: String?): HttpResponse<*> =
      AuthorId.fromString(id).nullToValidationError { listOf("invalid author id($id)") }
          .zipWith { manuscript(name, price, publish) }
          .validationErrorToHttpError
          .flatMap { pair -> writingNewBook.apply(pair) }
          .run(
              onSuccess = { logger.info("writeBook: success, author: {}, book: {}", id, it.id) },
              onFailure = { logger.info("writeBook: failure, author: {}, status: {}, error: {}", id, it.first, it.second) })
          .map { bookJson -> HttpResponse
              .created<BookJson>(URI.create("/authors/$id/books/${bookJson.id}"))
              .body(bookJson) as HttpResponse<*> }
          .rescue { it.toResponse() }

  companion object {
    private fun manuscript(
        name: String?,
        price: String?,
        publish: String?): ResultEx<ValidationError, Manuscript> =
        BookName.from(name).nullToValidationError { listOf("invalid name($name)") }
            .zipWith { Price.from(price).nullToValidationError { listOf("invalid price($price)") } }
            .zipWith { PublicationDate.from(publish).nullToValidationError { listOf("invalid publish date($publish)") } }
            .map { pair -> Manuscript(pair.first.first, pair.second, pair.first.second) }
  }
}
