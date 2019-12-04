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

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter

@Controller("/times")
class TimeController(private val clock: Clock, private val zoneRepository: ZoneRepository) {

  @Get("/default")
  fun getTime(): HttpResponse<Time> =
      HttpResponse.ok(Time(clock))
          .also { logger.info("request: {} at time: {}", it, it.body) }

  @Get("/{id}")
  fun getTime(@PathVariable("id") id: Int?): HttpResponse<*> =
      if (id == null) HttpResponse.badRequest(mapOf("message" to "bad id format"))
      else zoneRepository.findById(TimeZoneId(id))
          ?.let { HttpResponse.ok(Time(it.asOffset())) }
          ?: HttpResponse.notFound(mapOf("message" to "not found for id: $id"))

  companion object {
    val logger: Logger<TimeController> = Logger.get()
  }
}

data class Time(val timezone: String, val time: String) {
  constructor(clock: Clock) : this(clock.zone.id, OffsetDateTime.now(clock))
  constructor(offset: ZoneOffset) : this(offset, OffsetDateTime.now(offset))
  constructor(timezone: String, time: OffsetDateTime) : this(timezone, time.format(formatter))
  constructor(zoneOffset: ZoneOffset, time: OffsetDateTime) : this(zoneOffset.id, time.format(formatter))

  companion object {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  }
}

typealias FailedStatusReason = Pair<HttpStatus, Pair<String, String>>

fun ZoneId.asOffset(): ZoneOffset = this.rules.getOffset(Instant.now())

@Controller("/zones")
class ZoneController(private val zoneRepository: ZoneRepository) {

  private fun String.toZoneId(): ResultEx<FailedStatusReason, ZoneId> = try {
    ResultEx.success(ZoneId.of(this))
  } catch (ignored: DateTimeException) {
    ResultEx.failure(HttpStatus.BAD_REQUEST to ( "message" to "bad timezone: $this"))
  }

  private fun String.asUri(): URI = URI.create(this)

  @Post
  @Consumes("application/x-www-form-urlencoded")
  fun createNew(@Body("zone") zone: String): HttpResponse<*> =
      zone.toZoneId().flatMap {
        zoneRepository.save(it).asResultEx(HttpStatus.INTERNAL_SERVER_ERROR to ( "message" to "internal server error. please try later"))
      }.map { HttpResponse.created<Unit>("/zones/${it.value}".asUri()) as HttpResponse<*> }
          .rescue { HttpResponse.status<Map<String, String>>(it.first).body(mapOf(it.second)) }
}
