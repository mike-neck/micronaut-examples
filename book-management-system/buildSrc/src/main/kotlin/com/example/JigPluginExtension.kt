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

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File
import java.io.Serializable

open class JigPluginExtension(project: Project): Serializable {

  val workingDir: RegularFileProperty
  val outputDirectory: RegularFileProperty
  private val outputOmitPrefix: Property<String>
  private val jigModelPattern: Property<String>
  private val classes: RegularFileProperty
  private val resources: RegularFileProperty
  private val sources: RegularFileProperty
  val jigJar: RegularFileProperty

  init {
    val objects = project.objects
    this.workingDir = objects.fileProperty()
    this.workingDir.set(project.projectDir)
    this.outputDirectory = objects.fileProperty()
    this.outputDirectory.set(project.file("${project.buildDir}/jig"))
    this.outputOmitPrefix = objects.property(String::class.java)
    this.outputOmitPrefix.set(/* language=regex */".+\\\\.(service|domain\\\\.(model|type))\\\\.")
    this.jigModelPattern = objects.property(String::class.java)
    this.jigModelPattern.set(/* language=regex */".+\\\\.domain\\\\.(model|type)\\\\..+")
    this.classes = objects.fileProperty()
    this.classes.set(project.file("${project.buildDir}/classes/kotlin/main"))
    this.resources = objects.fileProperty()
    this.resources.set(project.file("${project.buildDir}/resources/main"))
    this.sources = objects.fileProperty()
    this.sources.set(project.file("src/main/kotlin"))
    this.jigJar = objects.fileProperty()
    this.jigJar.set(project.file("jig/jig.jar"))
  }

  val arguments: List<String> get() =
    listOf(
        workingDir.asFile.map { "--project.path=${it.absolutePath}" }.getOrElse(""),
        outputDirectory.asFile.map { "--outputDirectory=${it.absolutePath}" }.getOrElse(""),
        outputOmitPrefix.map { "--output.omit.prefix=$it" }.getOrElse(""),
        jigModelPattern.map { "--jig.model.pattern=$it" }.getOrElse(""),
        classes.asFile.map { "--directory.classes=${it.absolutePath}" }.getOrElse(""),
        resources.asFile.map { "--directory.resources=${it.absolutePath}" }.getOrElse(""),
        sources.asFile.map { "--directory.sources=${it.absolutePath}" }.getOrElse("")
    ).filter { it.isNotEmpty() }

  fun workingDir(dir: File) = workingDir.set(dir)
  fun outputDirectory(dir: File) = outputDirectory.set(dir)
  fun outputOmitPackagePrefix(/* language=regex */ pattern: String) = outputOmitPrefix.set(pattern)
  fun jigModelPackagePattern(/* language=regex */ pattern: String) = jigModelPattern.set(pattern)
  fun classesDir(dir: File) = classes.set(dir)
  fun resources(dir: File) = resources.set(dir)
  fun sources(dir: File) = sources.set(dir)
  fun jigJar(file: File) = jigJar.set(file)

  companion object {
    fun configure(project: Project): JigPluginExtension =
        project.extensions.create("jig", JigPluginExtension::class.java, project)
  }
}
