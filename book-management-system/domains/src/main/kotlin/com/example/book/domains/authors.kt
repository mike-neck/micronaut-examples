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

import com.example.attributes.AuthorFirstName
import com.example.attributes.AuthorLastName
import com.example.book.authors.AuthorId
import org.jetbrains.annotations.TestOnly

data class AuthorName(val firstName: AuthorFirstName, val lastName: AuthorLastName) {
  @TestOnly
  constructor(firstName: String, lastName: String): this(AuthorFirstName(firstName), AuthorLastName(lastName))

  companion object {
    fun fromPair(pair: Pair<AuthorFirstName, AuthorLastName>): AuthorName = AuthorName(pair.first, pair.second)
  }
}

data class Author(
    val id: AuthorId,
    val name: AuthorName
) {
  @TestOnly
  constructor(id: Long, name: AuthorName): this(AuthorId(id), name)
}
