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
package com.example.json

import com.example.attributes.*
import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.domains.PublishedBook
import com.example.book.authors.AuthorId
import com.example.book.books.BookId
import com.example.book.infra.entities.AuthorRecord
import java.time.format.DateTimeFormatter


data class AuthorJson(val id: Long, val firstName: String, val lastName: String) {
  constructor(author: Author): this(author.id, author.name)
  constructor(id: AuthorId, name: AuthorName): this(id, name.firstName, name.lastName)
  constructor(id: AuthorId, firstName: AuthorFirstName, lastName: AuthorLastName):
      this(id.value, firstName.value, lastName.value)
  constructor(author: AuthorRecord): this(author.id, author.firstName, author.lastName)

  companion object {
    operator fun invoke(authors: List<Author>): List<AuthorJson> =
        authors.map { author -> AuthorJson(author) }
  }
}

data class BookJson
(val id: Long, val name: String, val price: Int, val publish: String, val authors: List<AuthorJson>) {

  constructor(book: PublishedBook): this(book.id, book.name, book.price, book.publicationDate, book.authors.authors)
  constructor(id: BookId, name: BookName, price: Price, date: PublicationDate, authors: List<Author>):
      this(id.value, name.value, price.value, DateTimeFormatter.ISO_INSTANT.format(date.value), AuthorJson(authors))
}
