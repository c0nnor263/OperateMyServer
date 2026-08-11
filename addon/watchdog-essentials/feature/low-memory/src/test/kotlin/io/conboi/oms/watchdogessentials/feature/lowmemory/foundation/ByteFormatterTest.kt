package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ByteFormatterTest : ShouldSpec({

    context("format") {
        should("format bytes without unit prefix below KB") {
            ByteFormatter.format(0) shouldBe "0 B"
            ByteFormatter.format(512) shouldBe "512 B"
        }

        should("format kilobytes at KB boundary") {
            ByteFormatter.format(1024) shouldBe "1.0 KB"
            ByteFormatter.format(1536) shouldBe "1.5 KB"
        }

        should("format megabytes at MB boundary") {
            ByteFormatter.format(1024L * 1024L) shouldBe "1.0 MB"
            ByteFormatter.format(1_572_864L) shouldBe "1.5 MB"
        }

        should("format gigabytes at GB boundary") {
            ByteFormatter.format(1024L * 1024L * 1024L) shouldBe "1.00 GB"
            ByteFormatter.format(1_610_612_736L) shouldBe "1.50 GB"
        }
    }
})
