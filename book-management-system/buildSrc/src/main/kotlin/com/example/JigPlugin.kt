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

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project

open class JigPlugin : Plugin<Project> {

  override fun apply(project: Project) =
      configure(JigPluginExtension.configure(project), { jig ->
        project.tasks.create("downloadJigJar", Download::class.java) {
          it.group = "jig"
          it.src("https://github.com/dddjava/jig/releases/download/2019.12.2/jig-cli-kt.jar")
          it.dest(jig.jigJar.asFile)
          it.onlyIf { !jig.jigJar.asFile.get().exists() }
        }
      }, { jig ->
        project.tasks.create("jigReport", JigReportTask::class.java) {
          it.dependsOn("downloadJigJar")
          it.group = "jig"
          it.jig = jig
        }
      })

  companion object {
    fun <T> configure(with: T, vararg allConfigurations: (T) -> Unit): Unit =
        listOf(*allConfigurations).forEach { it.invoke(with) }
  }
}
