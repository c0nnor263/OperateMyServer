package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeExactly

class TpsCalculatorTest : ShouldSpec({

    context("calculateTps") {

        should("return DEFAULT_TPS for empty array") {
            val result = TpsCalculator.calculateTps(longArrayOf())
            result shouldBeExactly TpsCalculator.DEFAULT_TPS
        }

        should("calculate capped TPS for normal tick time") {
            val result = TpsCalculator.calculateTps(LongArray(100) { 50_000_000 })
            result shouldBeExactly 20.0
        }

        should("calculate lower TPS if tick time is high") {
            val result = TpsCalculator.calculateTps(LongArray(100) { 100_000_000 })
            result shouldBeExactly 10.0
        }

        should("not exceed DEFAULT_TPS even if tick time is low") {
            val result = TpsCalculator.calculateTps(LongArray(100) { 5_000_000 })
            result shouldBeExactly 20.0
        }

        should("fall back to DEFAULT_TPS for zero tick time") {
            val result = TpsCalculator.calculateTps(LongArray(100) { 0L })
            result shouldBeExactly TpsCalculator.DEFAULT_TPS
        }
    }

    context("calculateGlobalTps") {
        should("average the server TPS and level TPS samples") {
            TpsCalculator.calculateGlobalTps(
                serverTickTimes = LongArray(100) { 50_000_000 },
                levelTpsSamples = listOf(10.0, 20.0)
            ) shouldBeExactly 16.666666666666668
        }
    }
})
