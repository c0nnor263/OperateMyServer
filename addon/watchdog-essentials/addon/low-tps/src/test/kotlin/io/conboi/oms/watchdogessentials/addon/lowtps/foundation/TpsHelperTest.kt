package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeExactly

class TpsHelperTest : ShouldSpec({

    context("calculateTps") {

        should("return DEFAULT_TPS for empty array") {
            val result = TpsHelper.calculateTps(longArrayOf())
            result shouldBeExactly TpsHelper.DEFAULT_TPS
        }

        should("calculate capped TPS for normal tick time") {
            val result = TpsHelper.calculateTps(LongArray(100) { 50_000_000 })
            result shouldBeExactly 20.0
        }

        should("calculate lower TPS if tick time is high") {
            val result = TpsHelper.calculateTps(LongArray(100) { 100_000_000 })
            result shouldBeExactly 10.0
        }

        should("not exceed DEFAULT_TPS even if tick time is low") {
            val result = TpsHelper.calculateTps(LongArray(100) { 5_000_000 })
            result shouldBeExactly 20.0
        }
    }
})
