package io.conboi.oms.api.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class TickTimerTest : ShouldSpec({

    context("shouldFire") {

        should("fire every 20 ticks by default") {
            TickTimer().apply {
                shouldFire(serverTickCount = 20) shouldBe true
                shouldFire(serverTickCount = 40) shouldBe true
                shouldFire(serverTickCount = 21) shouldBe false
            }
        }

        should("fire at custom interval") {
            TickTimer(intervalTicks = 10).apply {
                shouldFire(serverTickCount = 10) shouldBe true
                shouldFire(serverTickCount = 20) shouldBe true
                shouldFire(serverTickCount = 15) shouldBe false
            }
        }
    }
})
