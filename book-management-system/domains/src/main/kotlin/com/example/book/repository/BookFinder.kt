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
package com.example.book.repository

import com.example.book.domains.PublishedBook
import com.example.book.authors.AuthorId
import com.example.book.books.BookId

interface BookFinder {
  fun findById(id: BookId): PublishedBook?
  fun findByAuthorIdAndByBookId(authorId: AuthorId, bookId: BookId): PublishedBook?
}
