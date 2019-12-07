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
package com.example.book

import java.lang.Exception
import java.lang.RuntimeException

class GatewayException(message: String, cause: Exception? = null): RuntimeException(message, cause)

sealed class ResultEx<FAILURE, SUCCESS> {
  abstract fun <NEXT> map(f: (SUCCESS) -> NEXT): ResultEx<FAILURE, NEXT>
  abstract fun <NEXT> flatMap(f: (SUCCESS) -> ResultEx<FAILURE, NEXT>): ResultEx<FAILURE, NEXT>
  abstract fun rescue(f: (FAILURE) -> SUCCESS): SUCCESS

  companion object {
    fun <F: Any, S: Any> S?.asResult(f: () -> F): ResultEx<F, S> = if (this == null) Failure(f()) else Success(this) 
  }
}

data class Success<FAILURE, SUCCESS>(val value: SUCCESS): ResultEx<FAILURE, SUCCESS>() {

  override fun <NEXT> map(f: (SUCCESS) -> NEXT): ResultEx<FAILURE, NEXT> = Success(f(value))

  override fun <NEXT> flatMap(f: (SUCCESS) -> ResultEx<FAILURE, NEXT>): ResultEx<FAILURE, NEXT> = f(value)

  override fun rescue(f: (FAILURE) -> SUCCESS): SUCCESS = value
} 

data class Failure<FAILURE, SUCCESS>(val value: FAILURE): ResultEx<FAILURE, SUCCESS>() {

  override fun <NEXT> map(f: (SUCCESS) -> NEXT): ResultEx<FAILURE, NEXT> = Failure(value)

  override fun <NEXT> flatMap(f: (SUCCESS) -> ResultEx<FAILURE, NEXT>): ResultEx<FAILURE, NEXT> = Failure(value)

  override fun rescue(f: (FAILURE) -> SUCCESS): SUCCESS = f(value)
} 

enum class Cause {
  NOT_FOUND,
  CONFLICT,
  BAD_PARAM,
  ;

  fun with(message: String): Reason = this to message
}

typealias Reason = Pair<Cause, String>
