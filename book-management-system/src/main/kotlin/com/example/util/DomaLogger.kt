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
package com.example.util

import org.seasar.doma.jdbc.AbstractJdbcLogger
import org.seasar.doma.jdbc.Sql
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.function.Supplier

class DomaLogger: AbstractJdbcLogger<Level>(Level.INFO) {

  interface LogSender {
    fun onNormal(message: String)
    fun onException(message: String, exception: Throwable)
  }

  override fun logDaoMethodEntering(callerClassName: String?, callerMethodName: String?, args: Array<out Any>?, level: Level?, messageSupplier: Supplier<String>?) =
      log(Level.DEBUG, callerClassName, callerMethodName, null, messageSupplier)

  override fun logDaoMethodExiting(callerClassName: String?, callerMethodName: String?, result: Any?, level: Level?, messageSupplier: Supplier<String>?) =
      log(Level.DEBUG, callerClassName, callerMethodName, null, messageSupplier)

  override fun logSql(callerClassName: String?, callerMethodName: String?, sql: Sql<*>?, level: Level?, messageSupplier: Supplier<String>?) =
      log(Level.DEBUG, callerClassName, callerMethodName, null, messageSupplier)

  override fun log(
      level: Level?,
      callerClassName: String?,
      callerMethodName: String?,
      throwable: Throwable?,
      messageSupplier: Supplier<String>?) =
      LoggerFactory.getLogger(callerClassName).let { logger ->
        val message = messageSupplier?.get() ?: ""
        val location = "class: ${callerClassName?:"unknown-class"}, method: ${callerMethodName?:"unknown-method"}"
        val logText = "message: $message, $location"
        val logSender: LogSender = when (level ?: Level.INFO) {
          Level.INFO -> object : LogSender {
            override fun onNormal(message: String) = logger.info(message)
            override fun onException(message: String, exception: Throwable) = logger.info("{}, exception: {}", message, exception.toString(), exception)
          }
          Level.ERROR -> object : LogSender {
            override fun onNormal(message: String) = logger.error(message)
            override fun onException(message: String, exception: Throwable) = logger.error("{}, exception: {}", message, exception.toString(), exception)
          }
          Level.WARN -> object : LogSender {
            override fun onNormal(message: String) = logger.warn(message)
            override fun onException(message: String, exception: Throwable) = logger.warn("{}, exception: {}", message, exception.toString(), exception)
          }
          Level.DEBUG -> object : LogSender {
            override fun onNormal(message: String) = logger.debug(message)
            override fun onException(message: String, exception: Throwable) = logger.debug("{}, exception: {}", message, exception.toString(), exception)
          }
          Level.TRACE -> object : LogSender {
            override fun onNormal(message: String) = logger.trace(message)
            override fun onException(message: String, exception: Throwable) = logger.trace("{}, exception: {}", message, exception.toString(), exception)
          }
        }
        if (throwable == null) logSender.onNormal(logText)
        else logSender.onException(logText, throwable)
      }
}
