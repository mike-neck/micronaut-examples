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

import com.example.book.Cause
import com.example.book.Change
import com.example.book.Reason
import com.example.book.ResultEx
import com.example.book.ResultEx.Companion.asResult
import com.example.book.domains.*
import com.example.book.ids.BookId
import com.example.book.infra.MicronautDomaConfigInjection
import com.example.book.infra.entities.BookAggregate
import com.example.book.infra.entities.BookRecord
import org.seasar.doma.*
import org.seasar.doma.experimental.Sql
import org.seasar.doma.jdbc.Result

@Dao
@MicronautDomaConfigInjection
interface BookDao {
  @Sql(// language=sql
      """
    select * from books;
  """)
  @Select
  fun findAll(): List<BookRecord>

  @Sql(// language=sql
      """
        select * from BOOKS
        where ID = /* bookId */10;
  """)
  @Select
  fun findById(bookId: BookId): BookRecord?

  //language=sql
  @Sql("""
    select 
        b.ID as book_id,
        b.NAME as name,
        b.PRICE as price,
        b.PUBLICATION_DATE as publication_date,
        a.ID as author_id,
        a.FIRST_NAME as first_name,
        a.LAST_NAME as last_name
    from BOOKS as b
        join WRITINGS w on b.ID = w.BOOK_ID
        join AUTHORS a on w.AUTHOR_ID = a.ID
    where
        b.ID = /* bookId */1000
  """)
  @Select
  fun findBookAggregateById(bookId: BookId): List<BookAggregate>

  @Insert
  fun createNew(book: BookRecord): Result<BookRecord>

  @Update
  fun update(book: BookRecord): Result<BookRecord>

  @Delete
  fun delete(book: BookRecord): Result<BookRecord>
}

fun BookDao.deleteById(bookId: BookId): ResultEx<Reason, BookRecord> =
    findById(bookId).asResult { Cause.NOT_FOUND.with("not found(${bookId.value})") }
        .map { bookRecord -> delete(bookRecord) }
        .flatMap { result ->
          if (result.count == 0) ResultEx.failure<Reason, BookRecord>(Cause.CONFLICT.with("invalid state"))
          else ResultEx.success(result.entity) }

fun BookDao.findPublishedBookById(bookId: BookId): PublishedBook? {
  val list = this.findBookAggregateById(bookId)
  if (list.isEmpty()) {
    return null
  }
  val book = list[0]
  val authors = list.map { Author(it.authorId, AuthorName(it.firstName, it.lastName)) }
  return PublishedBook(book.bookId, book.name, book.publicationDate, book.price, Authors(authors))
}

fun BookDao.update(bookChange: BookChange): PublishedBook? {
  if (!bookChange.needUpdate) {
    return null
  }
  val bookRecord = this.findById(bookChange.id) ?: return null
  val newBookRecord = bookRecord.applyChange(bookChange)
  val result = this.update(newBookRecord)
  if (result.count != 1) {
    return null
  }
  return this.findPublishedBookById(newBookRecord.id)
}
