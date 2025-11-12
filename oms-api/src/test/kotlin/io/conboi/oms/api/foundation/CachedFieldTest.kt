package io.conboi.oms.api.foundation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CachedFieldTest : FunSpec({

    context("initialization") {

        test("should accept key and value producers") {
            val cachedField = CachedField(
                key = { "key" },
                value = { 1 }
            )
            cachedField.get() shouldBe 1
            cachedField.key() shouldBe "key"
            cachedField.value() shouldBe 1
        }

        test("should accept valueValidator") {
            val validator: (Int) -> Boolean = { it > 0 }
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                validator = validator
            )
            field.get() shouldBe 1
            field.validator shouldBe validator
        }

        test("should accept onUpdate callback") {
            val onUpdate: (Int?, Int) -> Unit = { _, _ -> }
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                onUpdate = onUpdate
            )
            field.get() shouldBe 1
            field.onUpdate shouldBe onUpdate
        }

        test("should accept observe flag") {
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                observe = true
            )
            field.observe shouldBe true
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

        test("should throw if validator fails on new value") {
            val field = CachedField(
                key = { "key" },
                value = { 1 },
                validator = { it > 1 }
            )

            shouldThrow<IllegalArgumentException> {
                field.get()
            }.message shouldBe "CachedField validation failed: value = 1"
        }

        test("should throw if validator fails after key changes") {
            var key = "key"
            var value = 0
            val field = CachedField(
                key = { key },
                value = { ++value },
                validator = { it < 2 }
            )

            field.get() shouldBe 1
            key = "newKey"

            shouldThrow<IllegalArgumentException> {
                field.get()
            }.message shouldBe "CachedField validation failed: value = 2"
        }

        test("should throw if value is not initialized yet") {
            val field = CachedField(
                key = { "key" },
                value = { error("no value") }
            )
            shouldThrow<IllegalStateException> {
                field.invalidate() // first init will fail
            }
        }

        test("should throw if key is same but cachedValue is null") {
            val field = CachedField(
                key = { "same-key" },
                value = { error("should not be called") },
                cachedKey = "same-key",
                cachedValue = null
            )

            val ex = shouldThrow<IllegalStateException> {
                field.get()
            }
            ex.message shouldBe "Cached value is not initialized"
        }

    }

    context("invalidate") {

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

        test("should refresh value on invalidate") {
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

        test("should update key on invalidate") {
            var value = 0
            val field = CachedField(
                key = { "key$value" },
                value = { ++value }
            )

            field.get() shouldBe 1
            field.key() shouldBe "key1"

            field.invalidate()
            field.get() shouldBe 2
            field.key() shouldBe "key2"

            field.invalidate()
            field.get() shouldBe 3
            field.key() shouldBe "key3"
        }
    }

    context("watch") {

        test("should call get when observe = true") {
            var called = false
            val field = CachedField(
                key = { "key" },
                value = {
                    called = true
                    1
                },
                observe = true
            )
            field.watch()
            called shouldBe true
        }

        test("should not call get when observe = false") {
            var called = false
            val field = CachedField(
                key = { "key" },
                value = {
                    called = true
                    1
                },
                observe = false
            )
            field.watch()
            called shouldBe false
        }
    }

    context("builder") {

        test("should build CachedField via builder DSL") {
            val field = cachedField {
                key = { "k" }
                value = { 42 }
                validator = { it > 0 }
                onUpdate = { _, _ -> }
                observe = true
            }

            field.get() shouldBe 42
            field.observe shouldBe true
            field.validator shouldNotBe null
            field.onUpdate shouldNotBe null
        }

        test("should build CachedField via builder") {
            val builder = CachedField.builder<String, Int>()
            builder.apply {
                key = { "k" }
                value = { 42 }
                validator = { it > 0 }
                onUpdate = { _, _ -> }
                observe = true
            }
            val field = builder.build()

            field.get() shouldBe 42
            field.observe shouldBe true
            field.validator shouldNotBe null
            field.onUpdate shouldNotBe null
        }

    }
})
