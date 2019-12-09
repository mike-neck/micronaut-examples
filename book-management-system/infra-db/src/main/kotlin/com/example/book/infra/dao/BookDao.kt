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
import com.example.book.infra.MicronautDomaConfigInjection
import com.example.book.infra.entities.BookRecord
import org.seasar.doma.Dao
import org.seasar.doma.Delete
import org.seasar.doma.Insert
import org.seasar.doma.Select
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

  @Insert
  fun createNew(book: BookRecord): Result<BookRecord>

  @Delete
  fun delete(book: BookRecord): Result<BookRecord>
}
