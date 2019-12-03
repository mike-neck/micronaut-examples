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

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Controller("/times")
class TimeController(private val clock: Clock) {

  @Get
  fun getTime(): Time = Time(clock).also { logger.info("request at time: {}", it) }

  companion object {
    val logger: Logger<TimeController> = Logger.get()
  }
}

data class Time(val timezone: String, val time: String) {
  constructor(clock: Clock): this(clock.zone.id, OffsetDateTime.now(clock))
  constructor(offset: ZoneOffset): this(offset, OffsetDateTime.now(offset))
  constructor(timezone: String, time: OffsetDateTime): this(timezone, time.format(formatter))
  constructor(zoneOffset: ZoneOffset, time: OffsetDateTime): this(zoneOffset.id, time.format(formatter))

  companion object {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  }
}

@Suppress("unused")
class Logger<T: Any>(delegate: org.slf4j.Logger): org.slf4j.Logger by delegate {
  companion object {
    inline fun <reified T: Any> get(): Logger<T> = Logger(LoggerFactory.getLogger(T::class.java)) 
  }
}
