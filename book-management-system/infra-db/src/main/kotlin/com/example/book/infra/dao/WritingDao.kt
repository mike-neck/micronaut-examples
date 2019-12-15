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

import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.domains.Authors
import com.example.book.domains.PublishedBook
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.infra.MicronautDomaConfigInjection
import com.example.book.infra.entities.BookAggregate
import com.example.book.infra.entities.WritingRecord
import org.seasar.doma.*
import org.seasar.doma.experimental.Sql
import org.seasar.doma.jdbc.BatchResult
import org.seasar.doma.jdbc.Result

@Dao
@MicronautDomaConfigInjection
interface WritingDao {

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
        w.AUTHOR_ID = /* authorId */2000
        and w.BOOK_ID = /* bookId */1000
  """)
  @Select
  fun findBookAggregate(authorId: AuthorId, bookId: BookId): BookAggregate?

  @BatchInsert
  fun createNew(writings: List<WritingRecord>): BatchResult<WritingRecord>

  @Insert
  fun createNew(writing: WritingRecord): Result<WritingRecord>

  @BatchDelete
  fun delete(writings: List<WritingRecord>): BatchResult<WritingRecord>

  @Delete
  fun delete(writing: WritingRecord): Result<WritingRecord>
}

fun WritingDao.findByAuthorIdAndByBookId(authorId: AuthorId, bookId: BookId): PublishedBook? =
    this.findBookAggregate(authorId, bookId)
        ?.let { PublishedBook(
            it.bookId,
            it.name,
            it.publicationDate,
            it.price,
            Authors(Author(it.authorId, AuthorName(it.firstName, it.lastName)))) }
