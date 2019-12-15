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
package com.example.http

import com.example.util.Cause
import com.example.util.Reason
import com.example.util.ResultEx
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

typealias ValidationError = List<String>

fun ValidationError.toHttpError(): HttpError = HttpStatus.BAD_REQUEST to this

val <T: Any> ResultEx<ValidationError, T>.validationErrorToHttpError: ResultEx<HttpError, T>
  get() =
    this.mapFailure { it.toHttpError() }

fun Reason.toHttpError(): HttpError =
    when (this.first) {
      Cause.NOT_FOUND -> HttpStatus.NOT_FOUND to listOf(this.second)
      Cause.CONFLICT -> HttpStatus.CONFLICT to listOf(this.second)
      Cause.BAD_PARAM -> HttpStatus.BAD_REQUEST to listOf(this.second)
    }

typealias HttpError = Pair<HttpStatus, List<String>>

fun <T: Any> T?.nullToHttpError(e: () -> HttpError): ResultEx<HttpError, T> =
    if (this != null) ResultEx.success(this)
    else ResultEx.failure(e())

fun HttpError.toResponse(): HttpResponse<Map<String, List<String>>> =
    HttpResponse.status<Map<String, List<String>>>(this.first)
        .body(mapOf("messages" to this.second))
