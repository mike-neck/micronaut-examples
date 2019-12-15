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

import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.infra.Db
import com.example.book.infra.DbExtension
import com.example.book.infra.entities.WritingRecord
import com.example.book.infra.executeUpdate
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.seasar.doma.jdbc.SqlExecutionException

@ExtendWith(DbExtension::class)
class WritingDaoTest {

  @Nested
  inner class CreateNewTest {
    @Test
    fun noBooksThenError() {
      assertThrows<SqlExecutionException> {
        Db.runOnNewTransaction {
          val writingDao = WritingDaoImpl(Db)
          val result = writingDao.createNew(WritingRecord(BookId(1000L), AuthorId(2000L)))
          fail("expected unreachable/ count:${result.count}/ content:${result.entity}")
        }
      }
    }

    @Test
    fun withBooksAndAuthorThenSuccess() {
      Db.runOnNewTransaction { 
        Db.connection().use { connection ->
          //language=sql
          """
          insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
          VALUES ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(connection)
          //language=sql
          """
          insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
          VALUES ( 2000, '三成', '石田' )
        """.trimIndent().executeUpdate(connection)
        }
      }

      Db.runOnNewTransaction {
        val writingDao = WritingDaoImpl(Db)
        val writing = WritingRecord(BookId(1000L), AuthorId(2000L))
        val result = writingDao.createNew(writing)
        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(writing, result.entity) }
        )
      }
    }
  }

  @Nested
  inner class DeleteTest {

    @Test
    fun noRecordsThenNoProblems() {
      Db.runOnNewTransaction { 
        val writingDao = WritingDaoImpl(Db)
        val writing = WritingRecord(BookId(1000L), AuthorId(2000L))
        val result = writingDao.delete(writing)
        assertAll(
            { assertEquals(0, result.count) },
            { assertEquals(writing, result.entity) }
        )
      }
    }

    @Test
    fun oneRecordsMatchesThenSuccess() {
      Db.runOnNewTransaction {
        Db.connection().use { conn ->
          //language=sql
          """
          insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
          VALUES ( 2000, '三成', '石田' )
        """.trimIndent().executeUpdate(conn)
          //language=sql
          """
          insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
          VALUES ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(conn)
          //language=sql
          """
          insert into WRITINGS (BOOK_ID, AUTHOR_ID) VALUES ( 1000, 2000 )
        """.trimIndent().executeUpdate(conn)
        }
      }

      Db.runOnNewTransaction {
        val writingDao = WritingDaoImpl(Db)
        val writing = WritingRecord(BookId(1000L), AuthorId(2000L))
        val result = writingDao.delete(writing)

        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(writing, result.entity) }
        )
      }
    }
  }
}
