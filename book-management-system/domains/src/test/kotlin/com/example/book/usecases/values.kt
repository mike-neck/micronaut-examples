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

import com.example.attributes.BookName
import com.example.attributes.Price
import com.example.attributes.PublicationDate
import com.example.book.domains.Author
import com.example.book.domains.AuthorName
import com.example.book.books.BookId
import java.time.Instant

object Values: ValueFactory

interface ValueFactory {
  val bookId get() = BookId(3000L)
  val bookName get() = BookName("罪と罰")
  val price get() = Price(3200)
  val publicationDate get() = PublicationDate(Instant.now())
  val author get() = Author(1000L, AuthorName("石田", "三成"))
}
