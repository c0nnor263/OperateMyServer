package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class MemoryUnitTest : ShouldSpec({

    context("constants") {
        should("define binary units") {
            MemoryUnit.KB shouldBe 1024L
            MemoryUnit.MB shouldBe 1024L * 1024L
            MemoryUnit.GB shouldBe 1024L * 1024L * 1024L
        }
    }

    context("mb") {
        should("convert megabytes to bytes") {
            MemoryUnit.mb(2) shouldBe 2L * MemoryUnit.MB
        }

        should("support zero and negative values") {
            MemoryUnit.mb(0) shouldBe 0L
            MemoryUnit.mb(-1) shouldBe -MemoryUnit.MB
        }
    }

    context("gb") {
        should("convert gigabytes to bytes") {
            MemoryUnit.gb(3) shouldBe 3L * MemoryUnit.GB
        }

        should("support zero and negative values") {
            MemoryUnit.gb(0) shouldBe 0L
            MemoryUnit.gb(-1) shouldBe -MemoryUnit.GB
        }
    }
})
