package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LevelTpsProviderTest : ShouldSpec({

    context("getAllLevelsTps") {
        should("collect only readable tick samples") {
            val samples = listOf(
                LongArray(100) { 100_000_000 },
                null,
                LongArray(100) { 50_000_000 }
            )

            LevelTpsProvider.getAllLevelsTps(samples) shouldBe listOf(10.0, 20.0)
        }
    }
})
