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

import com.example.book.domains.BookChange
import com.example.book.domains.PublishedBook
import com.example.book.domains.Work
import com.example.ids.IdGen
import com.example.book.infra.dao.BookDao
import com.example.book.infra.dao.WritingDao
import com.example.book.infra.dao.findPublishedBookById
import com.example.book.infra.dao.update
import com.example.book.infra.entities.BookRecord
import com.example.book.infra.entities.WritingRecord
import com.example.book.repository.BookFinder
import com.example.book.repository.BookWriteRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("command")
@Singleton
class BookWriterRepositoryImpl
@Inject constructor(
    private val idGen: IdGen,
    private val bookDao: BookDao,
    private val writingDao: WritingDao,
    @Named("query") private val bookFinder: BookFinder
) : BookWriteRepository, BookFinder by bookFinder{

  override fun save(work: Work): PublishedBook? {
    val bookRecord = BookRecord.fromWork(idGen, work)
    val result = bookDao.createNew(bookRecord)
    if (result.count != 1 || result.entity == null) {
      throw IllegalStateException("invalid book save state(result:${result.count})")
    }
    val writings = work.authors.authors.map {
      author -> WritingRecord(bookRecord.id, author.id) }
    val batchResult = writingDao.createNew(writings)
    if (batchResult.counts.any { it != 1 } ||
        batchResult.entities == null ||
        batchResult.entities.isEmpty()) {
      throw IllegalStateException("invalid writings save state(result:${batchResult.counts})")
    }
    return bookDao.findPublishedBookById(bookRecord.id)
  }

  override fun update(change: BookChange): PublishedBook? = bookDao.update(change)

  override fun delete(book: PublishedBook): Unit? {
    val writings = book.authors.authors.map { author -> WritingRecord(book.id, author.id) }
    val deleteWritingsResult = writingDao.delete(writings)
    if (deleteWritingsResult.counts.size != writings.size) {
      throw IllegalStateException("invalid writings delete state(result count un-match/expect:${writings.size},actual:${deleteWritingsResult.counts.size})")
    }
    val bookRecord = BookRecord(book.id, book.name, book.price, book.publicationDate)
    val result = bookDao.delete(bookRecord)
    if (result.count != 1) {
      throw IllegalStateException("invalid book delete state")
    }
    return Unit
  }
}
