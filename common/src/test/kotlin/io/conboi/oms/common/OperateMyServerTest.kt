package io.conboi.oms.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.apache.logging.log4j.Logger

class OperateMyServerTest : FunSpec({
    context("LOGGER") {
        test("should be initialized and of type Logger") {
            OperateMyServer.LOGGER.shouldBeInstanceOf<Logger>()
        }

        test("should have the correct name based on MOD_ID") {
            OperateMyServer.LOGGER.name shouldBe OperateMyServer.MOD_ID.uppercase()
        }
    }
})