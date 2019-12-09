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

import com.example.book.ids.BookId
import com.example.book.infra.*
import com.example.book.infra.entities.BookAggregate
import com.example.book.infra.entities.BookRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.seasar.doma.jdbc.UniqueConstraintException

@ExtendWith(DbExtension::class)
class BookDaoTest {

  private val logger: Logger<BookDaoTest> = Logger.get()

  @Test
  fun noDataThenEmptyList() {
    Db.transactionManager.requiresNew {
      val bookDao = BookDaoImpl(Db)
      val books = bookDao.findAll()
      assertTrue(books.isEmpty())
    }
  }

  @Test
  fun oneDataThenListHasOne() {
    Db.transactionManager.requiresNew {
      Db.connection().use { connection ->
        //language=sql
        """insert into BOOKS(id, name, price, publication_date)
        values ( 100, '罪と罰', 3200, '2019-12-11 13:00:00.123' )
        """.executeUpdate(connection)
      }
    }

    Db.transactionManager.requiresNew {
      val bookDao = BookDaoImpl(Db)
      val books = bookDao.findAll()
      assertEquals(listOf(BookRecord(100, "罪と罰", 3200, instant(2019, 12, 11, 13, 0, 0, 123_000_000))), books)
    }
  }

  @Nested
  inner class FindByIdTest {

    @Test
    fun noDataMatchesIdThenNull() {
      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = bookDao.findById(BookId(1000L))
        assertTrue(book == null)
      }
    }

    @Test
    fun oneRecordsMatchesIdThenNonNull() {
      Db.runOnNewTransaction {
        Db.connection().use { conn ->
          //language=sql
          """
          insert into BOOKS(id, name, price, publication_date)
          values ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.executeUpdate(conn)
          //language=sql
          """
          insert into BOOKS(id, name, price, publication_date)
          values ( 2000, 'カラマーゾフの兄弟', 4800, '2019-12-24 12:34:56.789' )
        """.trimIndent().executeUpdate(conn)
        }
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = bookDao.findById(BookId(1000L))
        assertAll(
            { assertTrue(book != null) },
            { assertEquals(BookRecord(1000, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000)), book) }
        )
      }
    }
  }

  @Nested
  inner class CreateNewTest {

    @Test
    fun noRecordsThenSuccess() {
      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000))

        val result = bookDao.createNew(book)

        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }

    @Test
    fun idConflict() {
      Db.runOnNewTransaction {
        Db.connection().use { conn ->
          //language=sql
          """
          insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
          values ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(conn)
        }
      }

      assertThrows<UniqueConstraintException> {
        Db.runOnNewTransaction {
          val bookDao = BookDaoImpl(Db)
          val book = BookRecord(1000, "カラマーゾフの兄弟", 4800, instant(2019, 12, 24, 12, 34, 56, 789_000_000))

          val result = bookDao.createNew(book)

          fail { "expected not to reach here/ result.count(${result.count}),result.content(${result.entity})" }
        }
      }
    }
  }

  @Nested
  inner class DeleteTest {

    @Test
    fun noRecordsThenNoProblem() {
      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000L, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000))
        val result = bookDao.delete(book)
        assertAll(
            { assertEquals(0, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }

    @Test
    fun oneRecordMatchesThenSuccess() {
      Db.runOnNewTransaction {
        Db.connection().use { connection ->
          //language=sql
          """
          insert into BOOKS(id, name, price, publication_date)
          values ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(connection)
        }
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000L, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000))
        val result = bookDao.delete(book)
        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }

    @Test
    fun afterUpdatedThenNoProblems() {
      // 楽観的ロックをかけたほうがいいかもしれんが、後回し
      Db.runOnNewTransaction {
        Db.connection().use { connection ->
          //language=sql
          """
          insert into BOOKS(id, name, price, publication_date)
          values ( 1000, 'カラマーゾフの兄弟', 4800, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(connection)
        }
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000L, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000))
        val result = bookDao.delete(book)
        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }
  }

  @Nested
  inner class UpdateTest {

    @Test
    fun noRecordsThenFailure() {
      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000L, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000))
        val result = bookDao.update(book)

        assertAll(
            { assertEquals(0, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }

    @Test
    fun oneRecordMatchesThenSuccess() {
      Db.runOnNewTransaction {
        Db.connection().use { connection ->
          //language=sql
          """
            insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
            VALUES ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
          """.trimIndent().executeUpdate(connection)
        }
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = BookRecord(1000L, "罪と罰", 4800, instant(2019, 12, 11, 12, 34, 56, 789_000_000))
        val result = bookDao.update(book)

        assertAll(
            { assertEquals(1, result.count) },
            { assertEquals(book, result.entity) }
        )
      }
    }
  }

  @Nested
  inner class FindAggregateTest {

    @Test
    fun noRecordsThenEmpty() {
      Db.runOnNewTransaction {
        prepareAuthors()
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val result = bookDao.findBookAggregateById(BookId(1000L))
        assertTrue(result.isEmpty())
      }
    }

    @Test
    fun singleAuthor() {
      Db.runOnNewTransaction {
        prepareAuthors()
        prepareBooks()
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val result = bookDao.findBookAggregateById(BookId(1000L))
        assertAll(
            { assertEquals(1, result.size) },
            {
              assertEquals(listOf(
                  BookAggregate(
                      1000L, "罪と罰", 3200, instant(2019, 12, 11, 12, 34, 56, 789_000_000),
                      2000L, "三成", "石田")
              ), result)
            }
        )
      }
    }

    @Test
    fun multipleAuthor() {
      Db.runOnNewTransaction {
        prepareAuthors()
        prepareBooks()
      }

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val result = bookDao.findBookAggregateById(BookId(1100L))
        assertEquals(3, result.size)
      }
    }

    private fun prepareAuthors() {
      Db.connection().use { connection ->
        //language=sql
        """
              insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
              VALUES ( 2000, '三成', '石田' )
            """.trimIndent().executeUpdate(connection)
        //language=sql
        """
              insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
              VALUES ( 2100, '元春', '吉川' )
            """.trimIndent().executeUpdate(connection)
        //language=sql
        """
          insert into AUTHORS (ID, FIRST_NAME, LAST_NAME)
          VALUES ( 2200, '恵瓊', '安国寺' )
        """.trimIndent().executeUpdate(connection)
      }
    }

    private fun prepareBooks() {
      Db.connection().use { connection ->
        //language=sql
        """
          insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE)
          VALUES ( 1000, '罪と罰', 3200, '2019-12-11 12:34:56.789' )
        """.trimIndent().executeUpdate(connection)
        //language=sql
        """
          insert into WRITINGS (BOOK_ID, AUTHOR_ID) VALUES ( 1000, 2000 )
        """.trimIndent().executeUpdate(connection)

        //language=sql
        """
          insert into BOOKS (ID, NAME, PRICE, PUBLICATION_DATE) 
          VALUES ( 1100, '実践 Kotlin', 2800, '2010-10-10 10:20:30.400' )
        """.trimIndent().executeUpdate(connection)
        //language=sql
        """
          insert into WRITINGS (BOOK_ID, AUTHOR_ID) VALUES
          ( 1100, 2000 ),
          ( 1100, 2100 ),
          ( 1100, 2200 )
        """.trimIndent().executeUpdate(connection)
      }
    }
  }
}
