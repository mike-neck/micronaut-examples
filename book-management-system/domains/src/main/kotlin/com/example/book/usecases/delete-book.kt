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
import com.example.book.ids.BookId
import com.example.book.repository.BookFinder
import com.example.book.repository.BookWriteRepository

import com.example.book.ResultEx.Companion.asResult
import javax.inject.Inject

class DeleteBook
@Inject constructor(private val bookFinder: BookFinder, private val bookWriteRepository: BookWriteRepository) {

  operator fun invoke(id: BookId): ResultEx<Reason, Unit> =
      bookFinder.findById(id)
          .asResult { Cause.NOT_FOUND.with("not found(${id.value})") }
          .flatMap { book ->
            bookWriteRepository.delete(book)
                .asResult { Cause.CONFLICT.with("failed to delete book(${id.value})") }
          }
}
