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

import com.example.book.ids.AuthorId
import com.example.book.infra.dao.AuthorDao
import com.example.book.usecases.AuthorsWritingNewBook
import com.example.book.usecases.CreateNewAuthor
import com.example.book.usecases.FindBooks
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.extensions.kotlintest.MicronautKotlinTestExtension.getMock
import io.mockk.every
import org.seasar.doma.internal.util.AssertionUtil.assertEquals

@MicronautTest
class AuthorControllerTest(
    private val createNewAuthor: CreateNewAuthor,
    private val writingNewBook: AuthorsWritingNewBook,
    private val findBooks: FindBooks,
    private val authorDao: AuthorDao,
    @Client("/authors") private val client: RxHttpClient
): BehaviorSpec({

  // TODO 書いたが動かない
  given("authorDao returns no record") {
    val mockAuthorDao = getMock(authorDao)
    every { mockAuthorDao.findById(AuthorId(1L)) } returns null
    `when`("curl http://example.com/authors/1") {
      val response = client.toBlocking().exchange<Unit>("/1")
      then("http status = 404") {
        assertEquals(404, response.code())
      }
    }
  }
})
