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

import io.kotlintest.TestCase
import io.kotlintest.TestType
import io.kotlintest.extensions.TestListener
import org.h2.tools.RunScript
import java.io.InputStreamReader
import java.sql.Connection
import javax.sql.DataSource

fun Connection.sql(sql: String): Int =
    Logger.get<Connection>().info("execute : {}", sql)
        .let { this.createStatement().executeUpdate(sql) }

class DbCleaner(private val dataSource: DataSource): TestListener {
  override fun beforeTest(testCase: TestCase) =
      when (testCase.name.startsWith("Given:") && testCase.type == TestType.Container) {
        true -> dataSource.connection.use { connection ->
          val stream = this::class.java.classLoader.getResourceAsStream("truncate.sql")
              ?: throw IllegalStateException("sql file not found")
          stream.use {
            RunScript.execute(connection, InputStreamReader(it, Charsets.UTF_8))
          }
        }.let { Unit }
        else -> Unit
      }
}
