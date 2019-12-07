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
package com.example.book.usecases

import io.kotlintest.TestContext

enum class TestPhase {
  GIVEN,
  WHEN,
  THEN,
  ;
}

fun TestContext.given() {
  val phase = this.metaData()["phase"] as? TestPhase
  assert(phase == null) { "given is called after ${phase?.name?.toLowerCase()}" }
  this.putMetaData("phase", TestPhase.GIVEN)
}

fun TestContext.`when`() {
  val phase = this.metaData()["phase"] as? TestPhase
  assert(phase != null) { "strange call order of when" }
  assert(phase == TestPhase.GIVEN) { "when is called after not 'given' but ${phase?.name?.toLowerCase()}" }
  this.putMetaData("phase", TestPhase.WHEN)
}

fun TestContext.then() {
  val phase = this.metaData()["phase"] as? TestPhase
  assert(phase != null) { "strange call order of then" }
  assert(phase == TestPhase.WHEN) { "then is called after not 'then' but ${phase?.name?.toLowerCase()}" }
  this.putMetaData("phase", TestPhase.THEN)
}

