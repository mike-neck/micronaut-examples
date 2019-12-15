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
import com.example.book.infra.MicronautDomaConfigInjection
import com.example.book.infra.entities.AuthorRecord
import org.seasar.doma.Dao
import org.seasar.doma.Insert
import org.seasar.doma.Select
import org.seasar.doma.experimental.Sql
import org.seasar.doma.jdbc.Result

@Dao
@MicronautDomaConfigInjection
interface AuthorDao {

  //language=sql
  @Sql("""
    select * from AUTHORS
    where ID = /* authorId */2000;
  """)
  @Select
  fun findById(authorId: AuthorId): AuthorRecord?

  @Insert
  fun save(author: AuthorRecord): Result<AuthorRecord>
}
