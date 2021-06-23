/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.OutputStream

internal class LogOutputStream(
    private val logger: Logger,
    private val level: LogLevel
) : OutputStream() {
    private var mem: StringBuffer = StringBuffer()

    override fun write(byte: Int) {
        if (byte.toChar() == '\n') {
            flush()
            return
        }

        mem = mem.append(byte.toChar())
    }

    override fun flush() {
        logger.log(level, mem.toString())
        mem = StringBuffer()
    }
}
