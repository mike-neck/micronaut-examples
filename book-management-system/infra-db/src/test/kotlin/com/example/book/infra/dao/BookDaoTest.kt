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
import com.example.book.infra.entities.BookTable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith

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
      val connection = Db.connection()
      //language=sql
      """insert into BOOKS(id, name, price, publication_date)
        values ( 100, '罪と罰', 3200, '2019-12-11 13:00:00.123' )
        """.executeUpdate(connection)
    }

    Db.transactionManager.requiresNew {
      val bookDao = BookDaoImpl(Db)
      val books = bookDao.findAll()
      assertEquals(listOf(BookTable(100, "罪と罰", 3200, instant(2019, 12, 11, 13, 0, 0, 123_000_000))), books)
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
        //language=sql
        val conn = Db.connection()
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

      Db.runOnNewTransaction {
        val bookDao = BookDaoImpl(Db)
        val book = bookDao.findById(BookId(1000L))
        assertAll(
            { assertTrue(book != null) },
            { assertEquals(BookTable(1000, "罪と罰", 3200, instant(2019,12,11,12,34,56,789_000_000)), book) }
        )
      }
    }
  }
}
