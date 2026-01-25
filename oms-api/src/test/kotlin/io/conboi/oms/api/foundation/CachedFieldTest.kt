package io.conboi.oms.api.foundation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CachedFieldTest : ShouldSpec({

    context("initialization") {

        should("initialize using key and value producers") {
            val field = CachedField(
                key = { "k" },
                value = { 42 }
            )

            field.get() shouldBe 42
            field.key() shouldBe "k"
        }

        should("store validator when provided") {
            val validator: (Int) -> Boolean = { it > 0 }

            val field = CachedField(
                key = { "k" },
                value = { 1 },
                validator = validator
            )

            field.validator shouldBe validator
            field.get() shouldBe 1
        }

        should("store onUpdate callback") {
            var called = false

            val field = CachedField(
                key = { "k" },
                value = { 1 },
                onUpdate = { _, _ -> called = true }
            )

            field.get()
            called shouldBe false
        }

        should("store observe flag") {
            val field = CachedField(
                key = { "k" },
                value = { 1 },
                observe = true
            )

            field.observe shouldBe true
        }
    }

    context("get") {

        should("return cached value when key does not change") {
            var counter = 0
            val field = CachedField(
                key = { "k" },
                value = { ++counter }
            )

            field.get() shouldBe 1
            field.get() shouldBe 1
        }

        should("recompute value when key changes") {
            var key = "a"
            var counter = 0
            val field = CachedField(
                key = { key },
                value = { ++counter }
            )

            field.get() shouldBe 1
            key = "b"
            field.get() shouldBe 2
        }

        should("call onUpdate when key changes") {
            var oldVal: Int? = null
            var newVal: Int? = null

            var key = "x"
            var counter = 0

            val field = CachedField(
                key = { key },
                value = { ++counter },
                onUpdate = { old, new ->
                    oldVal = old
                    newVal = new
                }
            )

            field.get() shouldBe 1
            oldVal.shouldBeNull()
            newVal.shouldBeNull()

            key = "y"
            field.get() shouldBe 2

            oldVal shouldBe 1
            newVal shouldBe 2
        }

        should("throw when validator fails on first get") {
            val field = CachedField(
                key = { "k" },
                value = { 1 },
                validator = { it > 1 }
            )

            val ex = shouldThrow<IllegalArgumentException> { field.get() }
            ex.message shouldBe "CachedField validation failed: value = 1"
        }

        should("throw when validator fails after key change") {
            var key = "a"
            var v = 0

            val field = CachedField(
                key = { key },
                value = { ++v },
                validator = { it < 2 }
            )

            field.get() shouldBe 1

            key = "b"
            val ex = shouldThrow<IllegalArgumentException> { field.get() }
            ex.message shouldBe "CachedField validation failed: value = 2"
        }

        should("throw when cachedKey matches but cachedValue is null") {
            val field = CachedField(
                key = { "same" },
                value = { error("should not run") },
                cachedKey = "same",
                cachedValue = null
            )

            val ex = shouldThrow<IllegalStateException> { field.get() }
            ex.message shouldBe "Cached value is not initialized"
        }
    }

    context("getSnapshotSafely") {

        should("return computed value when not cached yet") {
            val field = CachedField(
                key = { "k" },
                value = { 42 }
            )

            field.getSnapshotSafely() shouldBe 42
        }

        should("return cachedValue when available") {
            val field = CachedField(
                key = { "k" },
                value = { 42 }
            )

            field.get() shouldBe 42
            field.getSnapshotSafely() shouldBe 42
        }

        should("return null when value throws") {
            val field = CachedField(
                key = { "k" },
                value = { throw RuntimeException("fail") }
            )

            field.getSnapshotSafely().shouldBeNull()
        }

        should("return snapshot even after invalidate if cached") {
            var counter = 0
            val field = CachedField(
                key = { "k" },
                value = { ++counter }
            )

            field.get() shouldBe 1
            field.invalidate()
            field.getSnapshotSafely() shouldBe 2
        }
    }

    context("invalidate") {

        should("call onUpdate with old=null when invalidated before first get") {
            var oldVal: Int? = null
            var newVal: Int? = null

            var counter = 0

            val field = CachedField(
                key = { "k" },
                value = { ++counter },
                onUpdate = { old, new ->
                    oldVal = old
                    newVal = new
                }
            )

            field.invalidate()

            oldVal.shouldBeNull()
            newVal shouldBe 1
        }

        should("call onUpdate when invalidated after initialization") {
            var called = false

            val field = CachedField(
                key = { "k" },
                value = { 1 },
                onUpdate = { _, _ -> called = true }
            )

            field.get()
            called shouldBe false

            field.invalidate()
            called shouldBe true
        }

        should("refresh cached value on invalidate") {
            var counter = 0

            val field = CachedField(
                key = { "k" },
                value = { ++counter }
            )

            field.get() shouldBe 1
            field.invalidate()
            field.get() shouldBe 2
            field.invalidate()
            field.get() shouldBe 3
        }

        should("validate new value on invalidate") {
            var v = 0

            val field = CachedField(
                key = { "k" },
                value = { ++v },
                validator = { it < 2 }
            )

            field.get() shouldBe 1

            val ex = shouldThrow<IllegalArgumentException> { field.invalidate() }
            ex.message shouldBe "CachedField validation failed: value = 2"
        }
    }

    context("watch") {

        should("call get when observe=true") {
            var counter = 0
            val field = CachedField(
                key = { "k" },
                value = { ++counter },
                observe = true
            )

            field.watch()
            counter shouldBe 1
        }

        should("not call get when observe=false") {
            var counter = 0
            val field = CachedField(
                key = { "k" },
                value = { ++counter },
                observe = false
            )

            field.watch()
            counter shouldBe 0
        }
    }

    context("builder") {

        should("create CachedField via DSL") {
            val field = cachedField<String, Int> {
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

        should("create CachedField via explicit Builder") {
            val builder = CachedField.builder<String, Int>()

            builder.key = { "k" }
            builder.value = { 42 }
            builder.validator = { it > 0 }
            builder.onUpdate = { _, _ -> }
            builder.observe = true

            val field = builder.build()

            field.get() shouldBe 42
            field.observe shouldBe true
        }

        should("fail if builder fields missing") {
            val builder = CachedField.builder<String, Int>()
            val ex = shouldThrow<UninitializedPropertyAccessException> {
                builder.build()
            }
            ex.message shouldBe "lateinit property key has not been initialized"
        }
    }
})
