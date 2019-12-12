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

import com.example.DbCleaner
import com.example.IntegrationBehaviorSpec
import com.example.book.ids.IdGen
import com.example.json.AuthorJson
import com.example.json.BookJson
import com.example.sql
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.sql.DataSource

@MicronautTest
class AuthorControllerGetAuthorTest(
    override val dataSource: DataSource,
    @Client("/authors") private val client: RxHttpClient
) : IntegrationBehaviorSpec({

  given("For GET /authors/{id}, no authors saved") {
    `when`("curl http://example.com/authors/1") {
      val response = client.toBlocking().runCatching { exchange<Unit>("/1") }
      then("http status = 404") {
        val responseException = response.exceptionOrNull() as HttpClientResponseException
        responseException.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("For GET /authors/{id}, authors saved") {
    dataSource.connection.use {
      //language=sql
      it.sql("""
        insert into AUTHORS (ID, FIRST_NAME, LAST_NAME) VALUES ( 2000, '三成', '石田' )
      """.trimIndent())
      it.commit()
    }
    `when`("curl http://example.com/authors/2000") {
      val response = client.toBlocking().exchange<Unit>("/2000")
      then("http status = 200") {
        response.status shouldBe HttpStatus.OK
      }
    }
  }
}) {
  override fun listeners(): List<TestListener> = listOf(DbCleaner(dataSource))
}

@MicronautTest
class AuthorControllerCreateNewAuthorTest(
    override val dataSource: DataSource,
    private val idGen: IdGen,
    @Client("/authors") private val client: RxHttpClient
) : IntegrationBehaviorSpec({
  given("no authors saved") {
    `when`("""curl -X POST http://example.com/authors -d '{"firstName":"三成","lastName":"石田"}' -H 'content-type:application/json'""") {
      val post = HttpRequest
          .POST("", """{"firstName":"三成","lastName":"石田"}""")
          .header("content-type", "application/json")
      val result = client.toBlocking().exchange<String, AuthorJson>(post)
      then("http status = 201") {
        result.status shouldBe HttpStatus.CREATED
      }
      val expectedId = idGen.newLongId() - 1
      then("http location header = /authors/$expectedId") {
        result.header("location") shouldBe "/authors/$expectedId"
      }
    }
  }

  given("some authors saved") {
    dataSource.connection.use { connection ->
      listOf("三成" to "石田", "元春" to "吉川", "恵瓊" to "安国寺").forEach { name ->
        //language=sql
        connection.sql("""
          insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
          VALUES ( ${idGen.newLongId()}, '${name.first}', '${name.second}' )
        """.trimIndent())
      }
    }
    `when`("""curl -X POST http://example.com/authors -d '{"fistName":"秀家","lastName":"宇喜多"}' -H 'content-type:application/json'""") {
      val post = HttpRequest
          .POST("", """{"firstName":"秀家","lastName":"宇喜多"}""")
          .header("content-type", "application/json")
      val result = client.toBlocking().exchange<String, AuthorJson>(post)
      then("http status = 201") {
        result.status shouldBe HttpStatus.CREATED
      }
      val expectedId = idGen.newLongId() - 1
      then("http location header = /authors/$expectedId") {
        result.header("Location") shouldBe "/authors/$expectedId"
      }
    }
  }
})

@MicronautTest
class AuthorControllerWriteBookTest(
    override val dataSource: DataSource,
    private val idGen: IdGen,
    @Client("/authors") private val client: RxHttpClient
) : IntegrationBehaviorSpec({

  given("no authors saved") {
    `when`("""curl -X POST http://example.com/authors/1/books -d '{"name":"罪と罰","price":3200,"publish":"2019-12-11T12:34:56.789Z"}' -H 'content-type:application/json'""") {
      val request = HttpRequest
          .POST("1/books", """{"name":"罪と罰","price":3200,"publish":"2019-12-11T12:34:56.789Z"}""")
          .contentType("application/json")
      val result = client.toBlocking()
          .runCatching { exchange<String, BookJson>(request) }
      then("http status is not 2xx") {
        result.isSuccess shouldBe false
      }
      then("http status is 404") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("exception is expected to be not null, but null")
        val hcr = ex as? HttpClientResponseException
            ?: throw AssertionError("expected to be instance of HttpClientResponseException, but ${ex.javaClass}")
        hcr.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("author exists") {
    val authorId = idGen.newLongId()
    dataSource.connection.use { connection ->
      //language=sql
      connection.sql("""
        insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
        VALUES ( $authorId, '三成', '石田' )
      """.trimIndent())
    }
    `when`("""curl -X POST http://example.com/authors/$authorId/books -d '{"name":"罪と罰","price":3200,"publish":"2019-12-11T12:34:56.789Z"}' -H 'content-type:application/json'""") {
      val request = HttpRequest
          .POST("$authorId/books", """{"name":"罪と罰","price":3200,"publish":"2019-12-11T12:34:56.789Z"}""")
          .contentType("application/json")
      val result = client.toBlocking().exchange<String, BookJson>(request)
      then("http status is 201") {
        result.status shouldBe HttpStatus.CREATED
      }
      val expectedBookId = idGen.newLongId() - 1
      then("location = /authors/$authorId/books/$expectedBookId") {
        result.header("location") shouldBe "/authors/$authorId/books/$expectedBookId"
      }
    }
  }
})

@MicronautTest
class AuthorControllerGetAuthorsBookTest(
    override val dataSource: DataSource,
    private val idGen: IdGen,
    @Client("/authors") private val client: RxHttpClient
) : IntegrationBehaviorSpec({

  given("no authors saved") {
    `when`("""curl http://example.com/authors/1/books/2""") {
      val result = client.toBlocking()
          .runCatching { exchange<Unit>("/1/books/2") }
      then("http status is 404") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("expected error, but not")
        val httpError = ex as? HttpClientResponseException
            ?: throw AssertionError("expected to be instance of HttpClientResponseException, but ${ex.javaClass}")
        httpError.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("author exists but book not exists") {
    dataSource.connection.use { connection ->  
      //language=sql
      connection.sql("""
        insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
        VALUES ( 2000, '三成', '石田' )
      """.trimIndent())
    }
    `when`("""curl http://example.com/authors/1/books/2""") {
      val result = client.toBlocking()
          .runCatching { exchange<Unit>("/1/books/2") }
      then("http status is 404") {
        val ex = result.exceptionOrNull() ?: throw AssertionError("expected error, but not")
        val httpError = ex as? HttpClientResponseException
            ?: throw AssertionError("expected to be instance of HttpClientResponseException, but ${ex.javaClass}")
        httpError.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("author and book existing") {
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
    `when`("""curl http://example.com/authors/$authorId/books/$bookId""") {
      val result = client.toBlocking()
          .exchange<BookJson>("/$authorId/books/$bookId")
      then("http status is 200") {
        result.status shouldBe HttpStatus.OK
      }
    }
  }
})
