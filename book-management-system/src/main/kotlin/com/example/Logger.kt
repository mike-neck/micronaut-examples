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

import org.slf4j.LoggerFactory

@Suppress("unused")
class Logger<T: Any>(private val delegate: org.slf4j.Logger): org.slf4j.Logger by delegate {
  companion object {
    inline fun <reified T: Any> get(): Logger<T> = Logger(LoggerFactory.getLogger(T::class.java))
  }
}
