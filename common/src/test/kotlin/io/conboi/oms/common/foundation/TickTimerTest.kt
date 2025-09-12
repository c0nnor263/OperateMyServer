package io.conboi.oms.common.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


class TickTimerTest : FunSpec({

    context("shouldFire") {
        test("should return true when timer internalTicks is 20 and serverTickCount is 20") {
            val timer = TickTimer(20)
            timer.shouldFire(20) shouldBe true
        }

        test("should return true when timer internalTicks is 10 and serverTickCount is 20") {
            val timer = TickTimer(10)
            timer.shouldFire(20) shouldBe true
        }

        test("should return false when timer internalTicks is 30 and serverTickCount is 20") {
            val timer = TickTimer(30)
            timer.shouldFire(20) shouldBe false
        }
    }

})