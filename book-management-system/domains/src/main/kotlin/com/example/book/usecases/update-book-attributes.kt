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

import com.example.util.Cause
import com.example.util.Reason
import com.example.util.ResultEx
import com.example.book.domains.BookUpdate
import com.example.book.domains.PublishedBook
import com.example.book.books.BookId
import com.example.book.repository.BookFinder
import com.example.book.repository.BookWriteRepository

import com.example.util.ResultEx.Companion.asResult
import com.example.book.domains.BookChange
import javax.inject.Inject
import javax.inject.Named

class UpdateBookAttributes
@Inject constructor(
    @Named("query") private val bookFinder: BookFinder,
    @Named("command") private val bookWriteRepository: BookWriteRepository) {

  operator fun invoke(id: BookId, update: BookUpdate): ResultEx<Reason, PublishedBook> =
      bookFinder.findById(id).asResult { Cause.NOT_FOUND.with("not found(${id.value})") }
          .map { book -> book.accept(update) }

          .flatMap<BookChange> { change ->
            if (change.needUpdate) ResultEx.success(change)
            else ResultEx.failure(Cause.BAD_PARAM.with("no update")) }

          .flatMap { change -> bookWriteRepository.update(change)
              .asResult { Cause.CONFLICT.with("failed to update book(${id.value})") } }
}
