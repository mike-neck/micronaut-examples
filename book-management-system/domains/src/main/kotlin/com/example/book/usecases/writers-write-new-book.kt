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
package com.example.book.usecases

import com.example.book.Cause
import com.example.book.Reason
import com.example.book.ResultEx
import com.example.book.domains.Manuscript
import com.example.book.domains.PublishedBook
import com.example.book.ids.AuthorId
import com.example.book.repository.AuthorReadRepository

import com.example.book.ResultEx.Companion.asResult
import com.example.book.domains.Writers
import com.example.book.repository.BookWriterRepository

class WritersWritingNewBook(private val authorReadRepository: AuthorReadRepository, private val bookWriterRepository: BookWriterRepository) {

  fun <A: Any, B: Any> A?.onNull(f: () -> B): ResultEx<B, A> = this.asResult(f)

  fun apply(id: AuthorId, manuscript: Manuscript): ResultEx<Reason, PublishedBook> =
      authorReadRepository.findById(id).asResult { Cause.NOT_FOUND.with("not found author(${id.value})") }
          .map { person -> Writers(mainWriter = person) }
          .map { writers -> writers.write(manuscript) }
          .flatMap { work -> bookWriterRepository.save(work).asResult { Cause.CONFLICT.with("failed to publish book(name:${work.book.name.value})") } }
}
