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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class JigReportTask: DefaultTask() {

  @Input
  lateinit var jig: JigPluginExtension

  @get:OutputDirectory
  val outputDirectory: File? get() = jig.outputDirectory.asFile.orNull

  @TaskAction
  open fun execute() =
      project.javaexec { 
        it.args(jig.arguments)
        it.workingDir(jig.workingDir.asFile.getOrElse(project.projectDir))
        it.classpath(jig.jigJar.asFile.get())
        it.main = "org.springframework.boot.loader.JarLauncher"
      }
}
