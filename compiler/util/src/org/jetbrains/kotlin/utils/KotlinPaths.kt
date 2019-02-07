/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.utils

import java.io.File

interface KotlinPaths {
    val homePath: File

    val libPath: File

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.StdLib)"))
    val stdlibPath: File
        get() = jar(Jars.StdLib)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.reflect)"))
    val reflectPath: File
        get() = jar(Jars.Reflect)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.scriptRuntime)"))
    val scriptRuntimePath: File
        get() = jar(Jars.ScriptRuntime)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.kotlinTest)"))
    val kotlinTestPath: File
        get() = jar(Jars.KotlinTest)

    @Deprecated("Obsolete API", ReplaceWith("sourcesJar(KotlinPaths.Jars.stdLib)!!"))
    val stdlibSourcesPath: File
        get() = sourcesJar(Jars.StdLib)!!

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.jsStdLib)"))
    val jsStdLibJarPath: File
        get() = jar(Jars.JsStdLib)

    @Deprecated("Obsolete API", ReplaceWith("sourcesJar(KotlinPaths.Jars.JsStdLib)!!"))
    val jsStdLibSrcJarPath: File
        get() = sourcesJar(Jars.JsStdLib)!!

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.jsKotlinTest)"))
    val jsKotlinTestJarPath: File
        get() = jar(Jars.JsKotlinTest)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.allOpenPlugin)"))
    val allOpenPluginJarPath: File
        get() = jar(Jars.AllOpenPlugin)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.noArgPlugin)"))
    val noArgPluginJarPath: File
        get() = jar(Jars.NoArgPlugin)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.samWithReceiver)"))
    val samWithReceiverJarPath: File
        get() = jar(Jars.SamWithReceiver)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.trove4j)"))
    val trove4jJarPath: File
        get() = jar(Jars.Trove4j)

    @Deprecated("Obsolete API", ReplaceWith("classPath(KotlinPaths.ClassPaths.Compiler)"))
    val compilerClasspath: List<File>
        get() = classPath(ClassPaths.Compiler)

    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.compiler)"))
    val compilerPath: File
        get() = jar(Jars.Compiler)

    enum class Jars(val baseName: String) {
        StdLib(PathUtil.KOTLIN_JAVA_STDLIB_NAME),
        Reflect(PathUtil.KOTLIN_JAVA_REFLECT_NAME),
        ScriptRuntime(PathUtil.KOTLIN_JAVA_SCRIPT_RUNTIME_NAME),
        KotlinTest(PathUtil.KOTLIN_TEST_NAME),
        JsStdLib(PathUtil.JS_LIB_NAME),
        JsKotlinTest(PathUtil.KOTLIN_TEST_JS_NAME),
        AllOpenPlugin(PathUtil.ALLOPEN_PLUGIN_NAME),
        NoArgPlugin(PathUtil.NOARG_PLUGIN_NAME),
        SamWithReceiver(PathUtil.SAM_WITH_RECEIVER_PLUGIN_NAME),
        Trove4j(PathUtil.TROVE4J_NAME),
        Compiler(PathUtil.KOTLIN_COMPILER_NAME),
    }

    enum class ClassPaths(val contents: List<Jars> = emptyList()) {
        Empty(),
        Compiler(Jars.Compiler, Jars.StdLib, Jars.Reflect, Jars.ScriptRuntime, Jars.Trove4j),
        ;

        constructor(vararg jars: Jars) : this(jars.asList())
        constructor(baseClassPath: ClassPaths, vararg jars: Jars) : this(baseClassPath.contents + jars)
    }

    fun jar(jar: Jars): File
    
    fun sourcesJar(jar: Jars): File?

    fun classPath(jars: Sequence<Jars>): List<File> = jars.map(this::jar).toList()

    fun classPath(base: ClassPaths, vararg additionalJars: Jars): List<File> = classPath(base.contents.asSequence() + additionalJars)

    fun classPath(vararg jars: Jars): List<File> = classPath(jars.asSequence())
}

abstract class KotlinPathsFromBaseDirectory(val basePath: File) : KotlinPaths {

    override val libPath: File
        get() = basePath

    override fun jar(jar: KotlinPaths.Jars): File = basePath.resolve(jar.baseName + ".jar")

    override fun sourcesJar(jar: KotlinPaths.Jars): File? = basePath.resolve(jar.baseName + "-sources.jar")
}