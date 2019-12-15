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

import com.example.ids.IdGen
import com.example.util.DomaLogger
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.seasar.doma.jdbc.Config
import org.seasar.doma.jdbc.JdbcLogger
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.dialect.H2Dialect
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Singleton
@Factory
class ApplicationConfig {

  val atomicLong: AtomicLong = AtomicLong(0)

  @Bean
  fun idGen(): IdGen = object : IdGen {
    override fun newLongId(): Long = atomicLong.incrementAndGet()
  }
}

@Singleton
class DatabaseConfig
@Inject constructor(dataSource: DataSource): Config {

  private val injectedDataSource: DataSource = dataSource

  override fun getDataSource(): DataSource = injectedDataSource

  override fun getNaming(): Naming = Naming.SNAKE_LOWER_CASE

  override fun getDialect(): Dialect = h2Dialect

  override fun getJdbcLogger(): JdbcLogger = DomaLogger()

  companion object {
    val h2Dialect: Dialect = H2Dialect()
  }
} 
