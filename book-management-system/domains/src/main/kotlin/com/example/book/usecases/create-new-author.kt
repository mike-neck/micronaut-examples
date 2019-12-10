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
import com.example.book.ResultEx.Companion.asResult
import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.repository.AuthorWriteRepository
import javax.inject.Inject

class CreateNewAuthor
@Inject constructor(private val authorWriteRepository: AuthorWriteRepository) {
  operator fun invoke(authorName: AuthorName): ResultEx<Reason, Author> =
      authorWriteRepository.save(authorName)
          .asResult { Cause.CONFLICT.with("failed to save author(${authorName.firstName.value},${authorName.lastName.value})") }
}
