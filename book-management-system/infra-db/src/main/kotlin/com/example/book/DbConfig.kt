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
package com.example.book

import org.seasar.doma.jdbc.Config
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource
import org.seasar.doma.jdbc.tx.LocalTransactionManager
import org.seasar.doma.jdbc.tx.TransactionManager
import javax.sql.DataSource

object DbConfig : Config {

  private val dataSource get() = LocalTransactionDataSource("jdbc:h2:mem:app-dev", "app", "app")

  private val transactionManager = LocalTransactionManager(dataSource.getLocalTransaction(jdbcLogger))

  override fun getDataSource(): DataSource = dataSource

  override fun getNaming(): Naming = Naming.SNAKE_LOWER_CASE

  override fun getDialect(): Dialect = H2Dialect()

  override fun getTransactionManager(): TransactionManager = transactionManager
}
