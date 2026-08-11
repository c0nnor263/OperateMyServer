package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject

class RuntimeMemoryProviderTest : ShouldSpec({

    beforeSpec {
        mockkObject(TimeHelper)
    }

    beforeEach {
        every { TimeHelper.currentEpochSeconds } returns 1234L
    }

    afterEach {
        clearAllMocks()
    }

    context("snapshot") {
        should("return runtime memory snapshot with current epoch") {
            val runtime = Runtime.getRuntime()
            val snapshot = RuntimeMemoryProvider.snapshot()

            snapshot shouldBe MemorySnapshot(
                createdAt = 1234L,
                maxBytes = runtime.maxMemory(),
                allocatedBytes = runtime.totalMemory(),
                freeAllocatedBytes = runtime.freeMemory()
            )
        }
    }
})
