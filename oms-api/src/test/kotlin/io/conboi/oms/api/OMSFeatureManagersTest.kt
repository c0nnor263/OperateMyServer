package io.conboi.oms.api

import io.conboi.oms.api.foundation.feature.FeatureManager
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class OMSFeatureManagersTest : FunSpec({

    val mockFeatureManager: FeatureManager = mockk(relaxed = true)
    val omsFeatureManager = OMSFeatureManagers.oms

    fun clearRegistryPreservingOms() {
        val registryField = OMSFeatureManagers::class.java.getDeclaredField("registry")
        registryField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val registry = registryField.get(OMSFeatureManagers) as MutableMap<String, FeatureManager>
        registry.clear()
        registry["${OperateMyServer.MOD_ID}:main"] = omsFeatureManager

        val frozenField = OMSFeatureManagers::class.java.getDeclaredField("frozen")
        frozenField.isAccessible = true
        frozenField.setBoolean(OMSFeatureManagers, false)
    }

    beforeEach {
        clearRegistryPreservingOms()
        every { mockFeatureManager.modId } returns "testmod"
        every { mockFeatureManager.name } returns "testfeature"
        every { mockFeatureManager.getFullId() } returns "testmod:testfeature"
    }

    afterEach {
        clearAllMocks()
    }

    context("register") {

        test("should register a new FeatureManager") {
            OMSFeatureManagers.register(mockFeatureManager)
            OMSFeatureManagers.get<FeatureManager>("testmod:testfeature") shouldBe mockFeatureManager
        }

        test("should register a new FeatureManager with rich id") {
            every { mockFeatureManager.modId } returns "testmod_123-dev"
            every { mockFeatureManager.name } returns "main_manager"
            every { mockFeatureManager.getFullId() } returns "testmod_123-dev:main_manager"

            OMSFeatureManagers.register(mockFeatureManager)
            OMSFeatureManagers.get<FeatureManager>("testmod_123-dev:main_manager") shouldBe mockFeatureManager
        }

        test("should throw when registering a FeatureManager with duplicate id") {
            OMSFeatureManagers.register(mockFeatureManager)
            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager with id 'testmod:testfeature' is already registered"
        }

        test("should throw when modId is blank") {
            every { mockFeatureManager.modId } returns ""
            every { mockFeatureManager.name } returns "main"
            every { mockFeatureManager.getFullId() } returns ":main"

            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager id must be in the format 'modid:name'"
        }

        test("should throw when name is blank") {
            every { mockFeatureManager.modId } returns "testmod"
            every { mockFeatureManager.name } returns ""
            every { mockFeatureManager.getFullId() } returns "testmod:"

            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager id must be in the format 'modid:name'"
        }

        test("should throw when modId is reserved") {
            every { mockFeatureManager.modId } returns OperateMyServer.MOD_ID
            every { mockFeatureManager.name } returns "main"
            every { mockFeatureManager.getFullId() } returns "${OperateMyServer.MOD_ID}:main"

            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager with id '${OperateMyServer.MOD_ID}' cannot be registered, reserved for OMS"
        }

        test("should throw when id is blank") {
            every { mockFeatureManager.getFullId() } returns ""

            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager id cannot be blank"
        }

        test("should throw when id contains invalid characters") {
            every { mockFeatureManager.modId } returns "Invalid@ID"
            every { mockFeatureManager.name } returns "X"
            every { mockFeatureManager.getFullId() } returns "Invalid@ID:X"

            val exception = shouldThrow<IllegalArgumentException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "FeatureManager id can only contain lowercase letters, digits, underscores, hyphens and colons"
        }

        test("should throw when registry is frozen") {
            OMSFeatureManagers.freeze()

            val exception = shouldThrow<IllegalStateException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "Cannot register features after server has started!"
        }
    }

    context("get") {

    test("should retrieve registered FeatureManager by id") {
            OMSFeatureManagers.register(mockFeatureManager)
            val retrievedManager = OMSFeatureManagers.get<FeatureManager>("testmod:testfeature")
            retrievedManager shouldBe mockFeatureManager
        }

        test("should return null for unregistered FeatureManager id") {
            val retrievedManager = OMSFeatureManagers.get<FeatureManager>("nonexistent:feature")
            retrievedManager shouldBe null
        }
    }

    context("runForEach") {

        test("should run action for each registered FeatureManager") {
            OMSFeatureManagers.register(mockFeatureManager)
            var actionExecuted = false

            OMSFeatureManagers.runForEach {
                if (this == mockFeatureManager) actionExecuted = true
            }

            actionExecuted shouldBe true
        }
    }

    context("freeze") {

        test("should freeze registry and prevent future registrations") {
            OMSFeatureManagers.freeze()

            val exception = shouldThrow<IllegalStateException> {
                OMSFeatureManagers.register(mockFeatureManager)
            }
            exception.message shouldBe "Cannot register features after server has started!"
        }

        test("should be safe to call freeze multiple times") {
            OMSFeatureManagers.freeze()
            OMSFeatureManagers.freeze() // should not throw
        }

        test("should call freeze on all registered managers") {
            val mockFeatureManager2 = mockk<FeatureManager>(relaxed = true)
            every { mockFeatureManager2.modId } returns "mod"
            every { mockFeatureManager2.name } returns "feature"
            every { mockFeatureManager2.getFullId() } returns "mod:feature"

            OMSFeatureManagers.register(mockFeatureManager)
            OMSFeatureManagers.register(mockFeatureManager2)

            OMSFeatureManagers.freeze()

            verify { mockFeatureManager.freeze() }
            verify { mockFeatureManager2.freeze() }
        }
    }
})
