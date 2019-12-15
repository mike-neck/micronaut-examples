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

import com.example.book.domains.PublishedBook
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.infra.dao.BookDao
import com.example.book.infra.dao.WritingDao
import com.example.book.infra.dao.findByAuthorIdAndByBookId
import com.example.book.infra.dao.findPublishedBookById
import com.example.book.repository.BookFinder
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("query")
@Singleton
class BookFinderImpl
@Inject constructor(
    private val writingDao: WritingDao,
    private val bookDao: BookDao): BookFinder {

  override fun findById(id: BookId): PublishedBook? = bookDao.findPublishedBookById(id)

  override fun findByAuthorIdAndByBookId(authorId: AuthorId, bookId: BookId): PublishedBook? =
      writingDao.findByAuthorIdAndByBookId(authorId, bookId)
}
