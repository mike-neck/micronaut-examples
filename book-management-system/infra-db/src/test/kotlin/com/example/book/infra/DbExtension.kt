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
package com.example.book.infra

import org.h2.tools.RunScript
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.seasar.doma.jdbc.Config
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource
import org.seasar.doma.jdbc.tx.LocalTransactionManager
import org.seasar.doma.jdbc.tx.TransactionManager
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.InputStreamReader
import java.io.Reader
import java.sql.Connection
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.sql.DataSource

@Suppress("unused")
class Logger<T : Any>(private val logger: org.slf4j.Logger) : org.slf4j.Logger by logger {

  companion object {
    inline fun <reified T : Any> get(): Logger<T> = Logger(LoggerFactory.getLogger(T::class.java))
  }
}

class DbExtension : BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

  private val logger: Logger<DbExtension> = Logger.get()

  private fun resourceReader(resource: String): Reader? =
      if (Thread.currentThread().contextClassLoader.getResource(resource) == null) {
        null
      } else {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
        if (stream == null) null
        else InputStreamReader(stream, Charsets.UTF_8)
      }

  override fun beforeAll(context: ExtensionContext?) =
      SLF4JBridgeHandler.install().also { TimeZone.setDefault(TimeZone.getTimeZone("UTC")) }

  override fun beforeEach(context: ExtensionContext?) {
    logger.info("initializing database")
    resourceReader("db/migration/V201912081036__schema.sql").use { reader ->
      Db.transactionManager.required {
        RunScript.execute(Db.dataSource.connection, reader)
      }
    }
  }

  override fun afterEach(context: ExtensionContext?) {
    logger.info("dropping database")
    resourceReader("shutdown.sql").use { reader -> 
      Db.transactionManager.required { 
        RunScript.execute(Db.dataSource.connection, reader)
      }
    }
  }

}

object Db : Config {

  const val url = "jdbc:h2:mem:dev-app;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
  const val user = "app-dev"
  const val pass = "app-dev"

  private val ds = LocalTransactionDataSource(url, user, pass)
  private val tm = LocalTransactionManager(ds.getLocalTransaction(jdbcLogger))

  fun connection(): Connection = ds.connection

  fun runOnNewTransaction(runnable: () -> Unit) = transactionManager.required(runnable)

  override fun getDataSource(): DataSource = ds
  override fun getTransactionManager(): TransactionManager = tm

  override fun getNaming(): Naming = Naming.SNAKE_LOWER_CASE
  override fun getDialect(): Dialect = H2Dialect()
}

fun String.executeUpdate(conn: Connection): Int? =
    Logger.get<Db>().info("executing query[{}]", this).let {
      conn.createStatement().executeUpdate(this)
    }

fun instant(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minutes: Int, sec: Int, nano: Int): Instant =
    OffsetDateTime.of(year, month, dayOfMonth, hourOfDay, minutes, sec, nano, ZoneOffset.UTC).toInstant()
