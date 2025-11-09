package io.conboi.oms.utils.foundation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CachedFieldTest : FunSpec({

    context("initialization") {

        test("should accept key and value producers") {
            val cachedField = CachedField(
                key = { "key" },
                value = { 1 }
            )
            cachedField.get() shouldBe 1
        }

        test("should accept valueValidator") {
            val validator: (Int) -> Boolean = { it > 0 }
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                valueValidator = validator
            )
            field.get() shouldBe 1
        }

        test("should accept onUpdate callback") {
            val onUpdate: (Int?, Int) -> Unit = { _, _ -> }
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                onUpdate = onUpdate
            )
            field.get() shouldBe 1
        }
    }

    context("get") {

        test("should return cached value while key is unchanged") {
            var value = 0
            val field = CachedField(
                key = { "key" },
                value = { ++value }
            )
            field.get() shouldBe 1
            field.get() shouldBe 1
        }

        test("should recompute value if key changes") {
            var key = "key"
            var value = 0
            val field = CachedField(
                key = { key },
                value = { ++value }
            )
            field.get() shouldBe 1
            key = "newKey"
            field.get() shouldBe 2
            field.get() shouldBe 2
        }

        test("should call onUpdate when value changes") {
            var updatedOld: Int? = null
            var updatedNew: Int? = null
            var key = "key"
            var value = 0
            val field = CachedField(
                key = { key },
                value = { ++value },
                onUpdate = { old, new ->
                    updatedOld = old
                    updatedNew = new
                }
            )

            field.get() shouldBe 1
            updatedOld shouldBe null
            updatedNew shouldBe null

            key = "newKey"
            field.get() shouldBe 2
            updatedOld shouldBe 1
            updatedNew shouldBe 2
        }

        test("should throw if valueValidator fails on new value") {
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                valueValidator = { it > 1 }
            )
            shouldThrow<IllegalStateException> {
                field.get()
            }
        }

        test("should throw if validator fails after key changes") {
            var value = 0
            val field = CachedField(
                key = { "key$value" },
                value = { ++value },
                valueValidator = { it < 2 }
            )

            field.get() shouldBe 1
            shouldThrow<IllegalStateException> {
                field.get()
            }
        }

        test("should throw if value becomes null") {
            var value: Int? = 1
            val field = CachedField(
                key = { "key$value" },
                value = { value }
            )

            field.get() shouldBe 1
            value = null

            shouldThrow<IllegalStateException> {
                field.get()
            }
        }
    }

    context("invalidate()") {

        test("should call onUpdate with new value") {
            var updated = false
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                onUpdate = { _, _ -> updated = true }
            )
            field.get()
            updated shouldBe false
            field.invalidate()
            updated shouldBe true
        }

        test("should not fail if onUpdate is null") {
            val field = CachedField(
                key = { "key" },
                value = { 1 }
            )
            field.get()
            field.invalidate() // should not throw
        }

        test("should reset value on each invalidate") {
            var value = 0
            val field = CachedField(
                key = { "key" },
                value = { ++value }
            )

            field.get() shouldBe 1
            field.invalidate()
            field.get() shouldBe 2
            field.invalidate()
            field.get() shouldBe 3
        }
    }
})