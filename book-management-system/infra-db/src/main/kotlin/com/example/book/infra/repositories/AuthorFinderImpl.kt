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
import com.example.book.ids.AuthorId
import com.example.book.infra.dao.AuthorDao
import com.example.book.repository.AuthorFinder
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("query")
@Singleton
class AuthorFinderImpl @Inject constructor(private val authorDao: AuthorDao): AuthorFinder {

  override fun findById(id: AuthorId): Author? =
    authorDao.findById(id)?.let { Author(it.id, AuthorName(it.firstName, it.lastName)) }

}
