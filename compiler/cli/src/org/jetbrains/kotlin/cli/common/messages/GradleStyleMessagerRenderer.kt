/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.messages

class GradleStyleMessageRenderer : MessageRenderer {


    override fun render(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?): String {
        val result = StringBuilder()

        when (severity) {
            CompilerMessageSeverity.WARNING, CompilerMessageSeverity.STRONG_WARNING -> result.append("w: ")
            CompilerMessageSeverity.ERROR, CompilerMessageSeverity.EXCEPTION -> result.append("e: ")
            CompilerMessageSeverity.LOGGING, CompilerMessageSeverity.OUTPUT -> result.append("v: ")
            CompilerMessageSeverity.INFO -> result.append("i: ")
        }

        if (location != null) {
            val line = location.line
            val column = location.column

            result.append(location.path).append(": ")

            if (line > 0 && column > 0) {
                result.append("[").append(line).append(", ")
                result.append(column).append("]: ")
            }

            result.append(message)
        }

        return result.toString()
    }

    override fun renderPreamble() = ""

    override fun renderUsage(usage: String) = usage

    override fun renderConclusion() = ""

    override fun getName() = "GradleStyle"
}