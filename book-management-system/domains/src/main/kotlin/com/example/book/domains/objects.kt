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
package com.example.book.domains

import com.example.book.attributes.*
import com.example.book.ids.AuthorId
import com.example.book.ids.BookId
import org.jetbrains.annotations.TestOnly

data class AuthorName(val firstName: AuthorFirstName, val lastName: AuthorLastName) {
  @TestOnly constructor(firstName: String, lastName: String): this(AuthorFirstName(firstName), AuthorLastName(lastName))
}

data class Author(
    val id: AuthorId,
    val name: AuthorName
)

data class Writers(val mainWriter: Author, val coWriters: List<Author>) {
  constructor(mainWriter: Author, vararg coWriters: Author): this(mainWriter, listOf(*coWriters))

  private val asAuthors: Authors get() = Authors(mutableListOf(mainWriter) + coWriters)

  fun write(manuscript: Manuscript): Work = Work(manuscript, this.asAuthors)
}

data class Manuscript(
    val name: BookName,
    val publicationDate: PublicationDate,
    val price: Price)

data class Work(val book: Manuscript, val authors: Authors)

data class Authors(val authors: List<Author>)

data class PublishedBook(
    val id: BookId,
    val name: BookName,
    val publicationDate: PublicationDate,
    val price: Price,
    val authors: Authors)
