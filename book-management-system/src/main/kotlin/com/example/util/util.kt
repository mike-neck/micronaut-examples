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
package com.example.util

import com.example.book.Failure
import com.example.book.ResultEx
import com.example.book.Success
import com.example.http.ValidationError

fun <T: Any> T?.nullToValidationError(err: () -> ValidationError): ResultEx<ValidationError, T> =
    if (this == null) ResultEx.failure(err())
    else ResultEx.success(this)

fun <A: Any, B: Any> ResultEx<ValidationError, A>.zipWith(
    pair: () -> ResultEx<ValidationError, B>): ResultEx<ValidationError, Pair<A, B>> {
  val p = pair()
  return when(this) {
    is Success<ValidationError, A> -> {
      when(p) {
        is Success<ValidationError, B> -> ResultEx.success(this.value to p.value)
        is Failure<ValidationError, B> -> ResultEx.failure(p.value)
      }
    }
    is Failure<ValidationError, A> -> {
      when(p) {
        is Success<ValidationError, B> -> ResultEx.failure(this.value)
        is Failure<ValidationError, B> -> ResultEx.failure(this.value + p.value)
      }
    }
  }
}
