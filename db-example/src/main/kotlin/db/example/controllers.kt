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
package db.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import java.net.URI
import javax.inject.Inject

@Controller("books")
class BookController(private val idGen: IdGen) {

  @Inject
  lateinit var bookRepository: BookRepository

  @Post
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  fun save(@Body("title") title: String, @Body("pages") pages: Int): HttpResponse<*> =
      bookRepository.save(Book(title = title, pages = pages, id = idGen.newId()))
          .let { HttpResponse.created<Unit>(URI.create("/books/${it.id}")) }

  @Get("{id}")
  @Produces("application/json")
  fun findById(@PathVariable("id") id: Long): HttpResponse<*> =
      bookRepository.findById(id).kt.toResult(HttpStatus.NOT_FOUND to cause("not found for id: $id"))
          .map { book -> HttpResponse.ok(book) as HttpResponse<*> }
          .rescue { reason -> HttpResponse.status<Map<String, String>>(reason.first).body(mapOf(reason.second)) }

  @Delete("{id}")
  fun deleteById(@PathVariable("id") id: Long): HttpResponse<*> =
      bookRepository.findById(id).kt.toResult(HttpStatus.NOT_FOUND.withReason("not found for id: $id"))
          .map { book -> bookRepository.delete(book) }
          .map { HttpResponse.noContent<Map<String, Long>>().body(mapOf("id" to id)) as HttpResponse<*> }
          .rescue { HttpResponse.status<Map<String, String>>(it.first).body(mapOf(it.second)) }
}
