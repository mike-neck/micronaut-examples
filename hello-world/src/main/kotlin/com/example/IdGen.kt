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

import io.micronaut.context.annotation.Factory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

interface IdGen {
  fun generateNew(): Int
}

class AtomicIntIdGen(private val atomicInteger: AtomicInteger): IdGen {

  constructor(): this(AtomicInteger(0))

  override fun generateNew(): Int = atomicInteger.incrementAndGet()
}

@Factory
class AtomicIntIdGenFactory {
  @Singleton
  fun idGen(): IdGen = AtomicIntIdGen()
}
