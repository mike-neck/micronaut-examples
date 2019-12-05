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
package db.example

import io.micronaut.http.HttpStatus

sealed class ResultEx<E, S> {
  abstract fun <R> map(f: (S) -> R): ResultEx<E, R>
  abstract fun <R> flatMap(f: (S) -> ResultEx<E, R>): ResultEx<E, R>
  abstract fun rescue(f: (E) -> S): S

  companion object {
    fun <E, S> success(success: S): ResultEx<E, S> = Success(success)
    fun <E, S> error(error: E): ResultEx<E, S> = Failure(error)
  }
}

data class Success<E, S>(val success: S): ResultEx<E, S>() {
  override fun <R> map(f: (S) -> R): ResultEx<E, R> = Success(f(success))

  override fun <R> flatMap(f: (S) -> ResultEx<E, R>): ResultEx<E, R> = f(success)

  override fun rescue(f: (E) -> S): S = success
}

data class Failure<E, S>(val error: E): ResultEx<E, S>() {
  override fun <R> map(f: (S) -> R): ResultEx<E, R> = Failure(error)

  override fun <R> flatMap(f: (S) -> ResultEx<E, R>): ResultEx<E, R> = Failure(error)

  override fun rescue(f: (E) -> S): S = f(error)
}

fun <E: Any, S: Any> S?.toResult(err: E): ResultEx<E, S> = if (this == null) ResultEx.error(err) else ResultEx.success(this)

typealias Cause = Pair<String, String>
fun cause(message: String): Cause = "message" to message

typealias Reason = Pair<HttpStatus, Cause>
fun HttpStatus.withReason(message: String): Reason = this to cause(message)
