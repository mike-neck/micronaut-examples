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
package com.example

sealed class ResultEx<F, S> {
  abstract fun <R> map(f: (S) -> R): ResultEx<F, R>
  abstract fun <R> flatMap(f: (S) -> ResultEx<F, R>): ResultEx<F, R>
  abstract fun rescue(f: (F) -> S): S

  companion object {
    fun <F, S> success(s: S): ResultEx<F, S> = Success(s)
    fun <F, S> failure(e: F): ResultEx<F, S> = Failure(e)
  }
}

fun <S: Any, F> S?.asResultEx(f: F): ResultEx<F, S> = if (this == null) ResultEx.failure(f) else ResultEx.success(this)

private class Success<F, S>(private val s: S): ResultEx<F, S>() {

  override fun <R> map(f: (S) -> R): ResultEx<F, R> = Success(f(s))

  override fun <R> flatMap(f: (S) -> ResultEx<F, R>): ResultEx<F, R> = f(s)

  override fun rescue(f: (F) -> S): S = s
}

private class Failure<F, S>(private val e: F): ResultEx<F, S>() {

  override fun <R> map(f: (S) -> R): ResultEx<F, R> = Failure(e)

  override fun <R> flatMap(f: (S) -> ResultEx<F, R>): ResultEx<F, R> = Failure(e)

  override fun rescue(f: (F) -> S): S = f(e)
}
