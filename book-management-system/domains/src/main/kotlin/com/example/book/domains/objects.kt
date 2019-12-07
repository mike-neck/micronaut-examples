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

data class AuthorName(val firstName: AuthorFirstName, val lastName: AuthorLastName)

data class Person(
    val id: AuthorId,
    val name: AuthorName
)

data class Writers(val mainWriter: Person, val coWriter: List<Person>) {
  constructor(mainWriter: Person, vararg coWriter: Person): this(mainWriter, listOf(*coWriter))

  private val asAuthors: Authors get() = Authors(mutableListOf(mainWriter) + coWriter)

  fun write(manuscript: Manuscript): Work = Work(manuscript, this.asAuthors)
}

data class Manuscript(
    val name: BookName,
    val publicationDate: PublicationDate,
    val price: Price)

data class Work(val book: Manuscript, val authors: Authors)

data class Authors(val authors: List<Person>)

data class PublishedBook(
    val id: BookId,
    val name: BookName,
    val publicationDate: PublicationDate,
    val price: Price,
    val authors: Authors)
