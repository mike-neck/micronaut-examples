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
import org.gradle.api.tasks.JavaExec

open class JigPlugin : Plugin<Project> {

  override fun apply(project: Project) =
      all(JigPluginExtension.configure(project), { jig ->
        project.tasks.create("downloadJigJar", Download::class.java) {
          it.group = "jig"
          it.src("https://github.com/dddjava/jig/releases/download/2019.12.2/jig-cli-kt.jar")
          it.dest(jig.jigJar)
          it.onlyIf { !jig.jigJar.asFile.get().exists() }
        }
      }, { jig ->
        project.tasks.create("jigReport", JavaExec::class.java) {
          it.group = "jig"
          it.args = jig.arguments
          it.classpath = project.fileTree(jig.jigJar)
          it.main = "org.springframework.boot.loader.JarLauncher"
          it.workingDir(jig.workingDir)
        }
      })

  companion object {
    fun <T> all(ext: T, vararg configurations: (T) -> Unit): Unit =
        listOf(*configurations).forEach { it.invoke(ext) }
  }
}
