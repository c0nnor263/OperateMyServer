package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.names.DuplicateTestNameMode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.AssertionMode
import io.kotest.engine.concurrency.SpecExecutionMode
import io.kotest.engine.concurrency.TestExecutionMode

object ProjectConfig : AbstractProjectConfig() {
    override val specExecutionMode = SpecExecutionMode.Concurrent
    override val testExecutionMode = TestExecutionMode.Concurrent
    override val isolationMode: IsolationMode = IsolationMode.InstancePerRoot

    override val assertionMode = AssertionMode.Warn
    override val duplicateTestNameMode: DuplicateTestNameMode = DuplicateTestNameMode.Error
}