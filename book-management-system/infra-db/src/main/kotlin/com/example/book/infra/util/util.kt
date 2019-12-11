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
package com.example.book.infra.util

import com.example.book.ResultEx
import org.seasar.doma.jdbc.Result

fun <F: Any, S: Any> Result<S>.or(f: F): ResultEx<F, S> =
    when {
      this.count == 0 -> ResultEx.failure(f)
      this.entity == null -> ResultEx.failure(f)
      else -> ResultEx.success(this.entity)
    }

val <S: Any> Result<S>.orNull: S? get() =
    when {
      this.count == 0 -> null
      this.entity == null -> null
      else -> this.entity
    }

fun <S: Any, R: Any> Result<S>.map(f: (S) -> R): Result<R> =
    when {
      this.count == 0 -> Result<R>(0, null)
      this.entity == null -> Result<R>(this.count, null)
      else -> Result(this.count, f(this.entity))
    }
