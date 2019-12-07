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

import com.example.book.Change
import com.example.book.attributes.BookName
import com.example.book.attributes.Price
import com.example.book.attributes.PublicationDate
import com.example.book.ids.BookId
import org.jetbrains.annotations.TestOnly

data class PublishedBook(
    val id: BookId,
    val name: BookName,
    val publicationDate: PublicationDate,
    val price: Price,
    val authors: Authors) {

  fun accept(update: BookUpdate): BookChange =
      BookChange(
          id = id,
          name = Change(name, update.name),
          publicationDate = Change(publicationDate, update.publicationDate),
          price = Change(price, update.price)
      )
}



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

data class Authors(val authors: List<Author>) {
  @TestOnly
  constructor(vararg authors: Author): this(listOf(*authors))
}



data class BookUpdate(
    val name: BookName? = null,
    val publicationDate: PublicationDate? = null,
    val price: Price? = null
)

data class BookChange(
    val id: BookId,
    val name: Change<BookName>,
    val publicationDate: Change<PublicationDate>,
    val price: Change<Price>
) {
  val needUpdate: Boolean get() = arrayOf(name.hasChange, publicationDate.hasChange, price.hasChange).any { it }
}
