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

import com.example.IntegrationBehaviorSpec
import com.example.book.ids.IdGen
import com.example.json.BookJson
import com.example.sql
import io.kotlintest.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.sql.DataSource

@MicronautTest
class BookControllerGetBookTest(
    override val dataSource: DataSource,
    private val idGen: IdGen,
    @Client("/books") private val client: RxHttpClient
) : IntegrationBehaviorSpec ({

  given("no books saved") {
    `when`("""curl http://example.com/books/1""") {
      val result = client.toBlocking().runCatching { exchange<BookJson>("/1") }
      then("http status is 404") {
        val e = result.exceptionOrNull() ?: throw AssertionError("expected exception existing, but not")
        val httpException = e as? HttpClientResponseException
            ?: throw AssertionError("expected exception to be instance of HttpClientResponseException, but ${e.javaClass}")
        httpException.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("some books saved") {
    val authorId = idGen.newLongId()
    val bookId = idGen.newLongId()
    dataSource.connection.use { connection -> 
      //language=sql
      connection.sql("""
        insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
        VALUES ( $authorId, '三成', '石田' )
      """.trimIndent())
      //language=sql
      connection.sql("""
        insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
        VALUES ( $bookId, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
      """.trimIndent())
      //language=sql
      connection.sql("""
        insert into WRITINGS (BOOK_ID, AUTHOR_ID) VALUES ( $bookId, $authorId )
      """.trimIndent())
    }
    `when`("""curl http://example.com/books/$bookId""") {
      val result = client.toBlocking()
          .exchange<String>("/$bookId")
      then("http status is 200") {
        result.status shouldBe HttpStatus.OK
      }
    }
  }
})

@MicronautTest
class BookControllerUpdateBookTest(
    override val dataSource: DataSource,
    private val idGen: IdGen,
    @Client("/books") private val client: RxHttpClient
) : IntegrationBehaviorSpec ({

  given("no books saved") {
    `when`("""curl -X PATCH http://example.com/books/1 -d '{"name":"罪と罰 2nd edition"}' -H 'content-type:application/json'""") {
      val request = HttpRequest
          .PATCH("/1", """{"name":"罪と罰 2nd edition"}""")
          .contentType("application/json")
      val result = client.toBlocking()
          .runCatching { exchange<String, Any>(request) }
      then("http status is 404") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("expected exception but no exception")
        val httpError = ex as? HttpClientResponseException
            ?: throw AssertionError("expected exception to be instance of HttpClientResponseException, but ${ex.javaClass}")
        httpError.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("books saved") {
    val authorId = idGen.newLongId()
    val bookId = idGen.newLongId()
    dataSource.connection.use { connection ->
      //language=sql
      connection.sql("""
        insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
        VALUES ( $authorId, '三成', '石田' )
      """.trimIndent())
      //language=sql
      connection.sql("""
        insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
        VALUES ( $bookId, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
      """.trimIndent())
      //language=sql
      connection.sql("""
        insert into WRITINGS (BOOK_ID, AUTHOR_ID) VALUES ( $bookId, $authorId )
      """.trimIndent())
    }
    `when`("""curl -X PATCH http://example.com/books/$bookId -H 'content-type:application/json'""") {
      val request = HttpRequest
          .PATCH("/$bookId", "")
          .contentType("application/json")
      val result = client.toBlocking()
          .runCatching { exchange<String, Any>(request) }
      then("http status is 400") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("expected exception but no exception")
        val httpError = ex as? HttpClientResponseException
            ?: throw AssertionError("expected exception to be instance of HttpClientResponseException, but ${ex.javaClass}")
        httpError.status shouldBe HttpStatus.BAD_REQUEST
      }
    }

    `when`("""curl -X PATCH http://example.com/books/$bookId -d '{"name":"罪と罰"}' -H 'content-type:application/json'""") {
      val request = HttpRequest
          .PATCH("/$bookId", """{"name":"罪と罰"}""")
          .contentType("application/json")
      val result = client.toBlocking()
          .runCatching { exchange<String, Any>(request) }
      then("http status is 400(no changes)") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("expected exception but no exception")
        val httpError = ex as? HttpClientResponseException
            ?: throw AssertionError("expected exception to be instance of HttpClientResponseException, but ${ex.javaClass}")
        httpError.status shouldBe HttpStatus.BAD_REQUEST
      }
    }

    `when`("""curl -X PATCH http://example.com/books/$bookId -d '{"name":"罪と罰 2nd edition"}' -H 'content-type:application/json'""") {
      val request = HttpRequest
          .PATCH("/$bookId", """{"name":"罪と罰 2nd edition"}""")
          .contentType("application/json")
      val result = client.toBlocking().exchange<String, Any>(request)
      then("http status is 200") {
        result.status shouldBe HttpStatus.OK
      }
    }
  }
})
