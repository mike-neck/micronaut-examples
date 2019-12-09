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
package com.example.book.infra.repositories

import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.ids.AuthorId.Companion.newAuthorId
import com.example.book.ids.IdGen
import com.example.book.infra.dao.AuthorDao
import com.example.book.infra.entities.AuthorRecord
import com.example.book.infra.util.map
import com.example.book.infra.util.orNull
import com.example.book.repository.AuthorFinder
import com.example.book.repository.AuthorWriteRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("command")
@Singleton
class AuthorWriteRepositoryImpl
@Inject constructor(
    private val idGen: IdGen,
    private val authorDao: AuthorDao,
    @Named("query") private val authorFinder: AuthorFinder)
  : AuthorWriteRepository, AuthorFinder by authorFinder {

  override fun save(authorName: AuthorName): Author? =
      authorDao.save(AuthorRecord(idGen.newAuthorId(), authorName.firstName, authorName.lastName))
          .map { authorRecord -> Author(authorRecord.id, AuthorName(authorRecord.firstName, authorRecord.lastName)) }
          .orNull
} 
