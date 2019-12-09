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
package com.example.book.infra.dao

import com.example.book.attributes.AuthorFirstName
import com.example.book.attributes.AuthorLastName
import com.example.book.ids.AuthorId
import com.example.book.infra.Db
import com.example.book.infra.DbExtension
import com.example.book.infra.entities.AuthorRecord
import com.example.book.infra.executeUpdate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.seasar.doma.jdbc.UniqueConstraintException

@ExtendWith(DbExtension::class)
class AuthorDaoTest {

  @Nested
  inner class SaveTest {

    @Test
    fun canSaveAtEmptyTable() {
      Db.transactionManager.requiresNew {
        val authorDao: AuthorDao = AuthorDaoImpl(Db)
        val author = AuthorRecord(AuthorId(1000L), AuthorFirstName("三成"), AuthorLastName("石田"))
        val result = authorDao.save(author)
        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(author, result.entity) }
        )
      }
    }

    @Test
    fun cannotSaveDuplicateId() {
      Db.transactionManager.requiresNew {
        Db.connection().use { connection ->
          //language=sql
          "insert into AUTHORS (ID, FIRST_NAME, LAST_NAME) values (1000, '元春', '吉川');"
              .executeUpdate(connection)
        }
      }

      assertThrows<UniqueConstraintException> {
        Db.transactionManager.requiresNew {
          val authorDao: AuthorDao = AuthorDaoImpl(Db)
          val author = AuthorRecord(AuthorId(1000L), AuthorFirstName("三成"), AuthorLastName("石田"))
          val result = authorDao.save(author)
          fail("should fail at here/count: ${result.count}, data: ${result.entity}")
        }
      }
    }
  }

  @Nested
  inner class FindTest {

    @Test
    fun noRecordsThenNull() {
      Db.runOnNewTransaction {
        val authorDao = AuthorDaoImpl(Db)
        val author = authorDao.findById(AuthorId(2000))
        assertTrue(author == null)
      }
    }

    @Test
    fun oneRecordsMatches() {
      Db.runOnNewTransaction { 
        Db.connection().use { connection ->  
          //language=sql
          """
            insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
            VALUES ( 2000, '三成', '石田' )
          """.trimIndent().executeUpdate(connection)
        }
      }

      Db.runOnNewTransaction {
        val authorDao = AuthorDaoImpl(Db)
        val author = authorDao.findById(AuthorId(2000))
        val expected = AuthorRecord(AuthorId(2000L), AuthorFirstName("三成"), AuthorLastName("石田"))
        assertEquals(expected, author)
      }
    }
  }
}
