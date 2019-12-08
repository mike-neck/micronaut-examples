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
package com.example.book.conv

import com.example.book.attributes.AuthorFirstName
import com.example.book.attributes.AuthorLastName
import com.example.book.ids.AuthorId
import org.seasar.doma.ExternalDomain
import org.seasar.doma.jdbc.domain.DomainConverter

@ExternalDomain
class AuthorIdConverter: DomainConverter<AuthorId, Long> {
  override fun fromDomainToValue(domain: AuthorId?): Long = AuthorId.toPrimitiveValue(domain)

  override fun fromValueToDomain(value: Long?): AuthorId = AuthorId.strict(value)
}

@ExternalDomain
class AuthorFirstNameConverter: DomainConverter<AuthorFirstName, String> {
  override fun fromDomainToValue(domain: AuthorFirstName?): String = AuthorFirstName.toPrimitiveValue(domain)

  override fun fromValueToDomain(value: String?): AuthorFirstName = AuthorFirstName.strict(value)
} 

@ExternalDomain
class AuthorLastNameConverter: DomainConverter<AuthorLastName, String> {
  override fun fromDomainToValue(domain: AuthorLastName?): String = AuthorLastName.toPrimitiveValue(domain)

  override fun fromValueToDomain(value: String?): AuthorLastName = AuthorLastName.strict(value)
}
