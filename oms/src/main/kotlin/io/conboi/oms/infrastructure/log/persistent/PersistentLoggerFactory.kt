package io.conboi.oms.infrastructure.log.persistent

import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.AsyncAppender
import org.apache.logging.log4j.core.appender.RollingFileAppender
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig
import org.apache.logging.log4j.core.layout.PatternLayout

object PersistentLoggerFactory {
    fun get(name: String, dir: Path): Logger {
        Files.createDirectories(dir)

        val filePath = dir.resolve("$name.log").toString()

        val ctx = LogManager.getContext(false) as LoggerContext
        val config = ctx.configuration

        val layout = PatternLayout.newBuilder()
            .withPattern("[%d{HH:mm:ss}] [%level] %msg%n")
            .withConfiguration(config)
            .build()

        val rolling = RollingFileAppender.newBuilder()
            .withFileName(filePath)
            .withFilePattern("$filePath.%d{yyyy-MM-dd}")
            .withName("${name}_file")
            .withLayout(layout)
            .withAppend(true)
            .setConfiguration(config)
            .withPolicy(TimeBasedTriggeringPolicy.createPolicy("1", "true"))
            .withStrategy(
                DefaultRolloverStrategy.createStrategy(
                    "7",
                    null, null, null, null,
                    true,
                    config
                )
            )
            .build()

        rolling.start()
        config.appenders[rolling.name] = rolling


        val async = AsyncAppender.newBuilder()
            .setName("${name}_async")
            .setConfiguration(config)
            .setBlocking(false)
            .setBufferSize(1024)
            .setAppenderRefs(
                arrayOf(
                    AppenderRef.createAppenderRef(
                        rolling.name,
                        null,
                        null
                    )
                )
            )
            .build()

        async.start()

        config.appenders[async.name] = async


        val loggerConfig = LoggerConfig(name, Level.INFO, false)

        loggerConfig.addAppender(async, null, null)

        config.addLogger(name, loggerConfig)

        ctx.updateLoggers()
        return LogManager.getLogger(name)
    }
}
