package io.conboi.oms.common.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.ZonedDateTime

class TimeHelperTest : ShouldSpec({

    context("secondsBetween") {

        should("return positive difference when end is after start") {
            TimeHelper.secondsBetween(100, 130) shouldBe 30
        }

        should("return negative difference when start is after end") {
            TimeHelper.secondsBetween(200, 150) shouldBe -50
        }
    }

    context("secondsOfDay") {

        should("return modulo for positive values") {
            TimeHelper.secondsOfDay(3661) shouldBe 3661 // 1h 1m 1s
        }

        should("normalize negative modulo result") {
            TimeHelper.secondsOfDay(-100) shouldBe (86400 - 100)
        }
    }

    context("localMidnightEpoch") {

        should("compute correct midnight for given epoch and offset") {
            val zone = TimeHelper.zoneId

            val zdt = ZonedDateTime.of(
                2025, 10, 31,
                12, 0, 0, 0,
                zone
            )
            val epoch = zdt.toEpochSecond()
            val offset = zdt.offset.totalSeconds.toLong()

            val midnightLocalEpoch = TimeHelper.localMidnightEpoch(epoch, offset)

            val expectedUtcMidnightEpoch = zdt
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .toEpochSecond()

            (midnightLocalEpoch - offset) shouldBe expectedUtcMidnightEpoch
        }
    }

    context("closest") {

        should("return the closest future time when one exists") {
            TimeHelper.closest(
                1000L,
                listOf(LocalTime.of(0, 20), LocalTime.of(0, 40))
            ).let { result ->
                result shouldBe TimeHelper.closest(
                    1000L, listOf(
                        LocalTime.parse("00:20")
                    )
                )
            }
        }

        should("wrap to tomorrow when all times are earlier than now") {
            val zone = TimeHelper.zoneId

            val nowEpoch = ZonedDateTime.of(
                2025, 10, 31,
                23, 50, 0, 0,
                zone
            ).toEpochSecond()

            val result = TimeHelper.closest(
                nowEpoch,
                listOf(LocalTime.of(0, 0))
            )

            val resultLocal = ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(result),
                zone
            )

            resultLocal.dayOfMonth shouldBe 1
            resultLocal.toLocalTime() shouldBe LocalTime.MIDNIGHT
        }

        should("pick exact match when time aligns to second") {
            val zone = TimeHelper.zoneId

            val now = ZonedDateTime.of(
                2025, 1, 1,
                5, 30, 0, 0,
                zone
            )
            val nowEpoch = now.toEpochSecond()

            val result = TimeHelper.closest(nowEpoch, listOf(LocalTime.of(5, 30)))

            val resTime = ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(result),
                zone
            ).toLocalTime()

            resTime shouldBe LocalTime.of(5, 30)
        }
    }

    context("midnightOfNextDay") {

        should("compute tomorrow's midnight correctly") {
            val zone = TimeHelper.zoneId

            val nowEpoch = ZonedDateTime.of(
                2025, 10, 31,
                12, 0, 0, 0,
                zone
            ).toEpochSecond()

            val expected = ZonedDateTime.of(
                2025, 11, 1,
                0, 0, 0, 0,
                zone
            ).toEpochSecond()

            TimeHelper.midnightOfNextDay(nowEpoch) shouldBe expected
        }
    }

    context("currentOffsetSeconds") {
        fun set(name: String, value: Any) {
            val field = TimeHelper::class.java.getDeclaredField(name)
            field.isAccessible = true
            field.set(TimeHelper, value)
        }

        should("recalculate offset when lastOffsetUpdateEpoch < 0") {
            // force invalid cache
            set("lastOffsetUpdateEpoch", -1L)
            set("cachedOffsetSeconds", 0)

            val newOffset = TimeHelper.currentOffsetSeconds(1)

            newOffset shouldBeGreaterThan Long.MIN_VALUE
        }


        should("not recalc offset within 60 seconds") {
            val t1 = TimeHelper.currentOffsetSeconds()
            val t2 = TimeHelper.currentOffsetSeconds()

            t1 shouldBe t2
        }

        should("recalc offset after 60 seconds passed") {
            val now = TimeHelper.currentTime
            val before = TimeHelper.currentOffsetSeconds()

            set("lastOffsetUpdateEpoch", now - 120) // force outdated cache

            val after = TimeHelper.currentOffsetSeconds(now + 120)

            after shouldBeGreaterThan Long.MIN_VALUE
            before shouldBe after
        }
    }
})
