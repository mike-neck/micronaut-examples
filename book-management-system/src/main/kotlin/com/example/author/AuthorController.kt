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
import com.example.book.ResultEx
import com.example.book.attributes.AuthorFirstName
import com.example.book.attributes.AuthorLastName
import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.ids.AuthorId
import com.example.book.infra.dao.AuthorDao
import com.example.book.infra.entities.AuthorRecord
import com.example.book.usecases.CreateNewAuthor
import com.example.http.*
import com.example.util.validationError
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
    private val authorDao: AuthorDao
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
          .validationError { listOf("invalid number format(id:$id)") }
          .validationErrorToHttpError
          .flatMap { authorDao.findById(it).nullToHttpError { HttpStatus.NOT_FOUND to listOf("author not found(id:$id)") } }
          .run(
              onSuccess = {},
              onFailure = { logger.info("getAuthor: failure, id: {}, error: {}", id, it.second) })
          .map { HttpResponse.ok(AuthorJson(it)) as HttpResponse<*> }
          .rescue { it.toResponse() }

  @Post
  @Consumes("application/json", "application/x-www-form-urlencoded")
  @Produces("application/json")
  fun createNewAuthor(
      @Body("firstName") firstName: String?,
      @Body("lastName") lastName: String?): HttpResponse<*> =
      AuthorFirstName.from(firstName).validationError { listOf("invalid firstName") }
          .zipWith { AuthorLastName.from(lastName).validationError { listOf("invalid lastName") } }
          .validationErrorToHttpError
          .flatMap { createNewAuthor.apply(AuthorName.fromPair(it)) }
          .run(
              onSuccess = { logger.info("createNewAuthor: success, author: {}", it) },
              onFailure = { logger.info("createNewAuthor: failure, firstName: {}, lastName: {}, error: {}", firstName, lastName, it.second) })
          .map { HttpResponse.created<AuthorJson>(URI.create("/authors/${it.id}")).body(it) as HttpResponse<*> }
          .rescue { it.toResponse() }
}

data class AuthorJson(val id: Long, val firstName: String, val lastName: String) {
  constructor(author: Author): this(author.id, author.name)
  constructor(id: AuthorId, name: AuthorName): this(id, name.firstName, name.lastName)
  constructor(id: AuthorId, firstName: AuthorFirstName, lastName: AuthorLastName):
      this(id.value, firstName.value, lastName.value)
  constructor(author: AuthorRecord): this(author.id, author.firstName, author.lastName)
}
