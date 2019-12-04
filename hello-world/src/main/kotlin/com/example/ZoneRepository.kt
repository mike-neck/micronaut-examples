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

import java.time.ZoneId

data class TimeZoneId(val value: Int)

interface ZoneRepository {
  fun save(zone: ZoneId): TimeZoneId?
  fun findById(timeZoneId: TimeZoneId): ZoneId?
  fun delete(timeZoneId: TimeZoneId): ZoneId?
}

class MapZoneRepository(private val map: MutableMap<TimeZoneId, ZoneId> = mutableMapOf(), private val idGen: IdGen): ZoneRepository {

  private fun IdGen.newId(): TimeZoneId = TimeZoneId(this.generateNew())

  override fun save(zone: ZoneId): TimeZoneId? = idGen.newId()
      .apply { map[this] = zone }

  override fun findById(timeZoneId: TimeZoneId): ZoneId? = map[timeZoneId]

  override fun delete(timeZoneId: TimeZoneId): ZoneId? = map.remove(timeZoneId)
}
