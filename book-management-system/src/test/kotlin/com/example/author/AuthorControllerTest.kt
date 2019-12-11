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
import com.example.book.usecases.AuthorsWritingNewBook
import com.example.book.usecases.CreateNewAuthor
import com.example.book.usecases.FindAuthors
import com.example.book.usecases.FindBooks
import com.example.sql
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.sql.DataSource

@MicronautTest
class AuthorControllerTest(
    private val createNewAuthor: CreateNewAuthor,
    private val writingNewBook: AuthorsWritingNewBook,
    private val findBooks: FindBooks,
    private val findAuthors: FindAuthors,
    private val dataSource: DataSource,
    @Client("/authors") private val client: RxHttpClient
) : BehaviorSpec({

  given("no records saved") {
    `when`("curl http://example.com/authors/1") {
      val response = client.toBlocking().runCatching { exchange<Unit>("/1") }
      then("http status = 404") {
        val responseException = response.exceptionOrNull() as HttpClientResponseException
        responseException.status shouldBe HttpStatus.NOT_FOUND
      }
    }
  }

  given("records saved") {
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
