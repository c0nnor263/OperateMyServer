package io.conboi.oms.api.infrastructure.file

import io.kotest.core.spec.style.FunSpec

class OMSRootPathTest : FunSpec({

//    lateinit var tempDir: Path
//
//    val mockServer = mockk<MinecraftServer>()
//
//    beforeEach {
//        tempDir = Files.createTempDirectory("oms_test")
//
//        every { mockServer.serverDirectory } returns tempDir.toFile()
//    }
//
//    afterEach {
//        val field = OMSRootPath::class.java.getDeclaredField("cachedRootPath")
//        field.isAccessible = true
//        field.set(null, null)
//
//        tempDir.toFile().deleteRecursively()
//
//        clearAllMocks()
//    }
//
//    context("init") {
//        test("should create oms directory and cache root") {
//            OMSRootPath.init(mockServer)
//
//            val expectedPath = tempDir.resolve("oms")
//            expectedPath.toFile().exists() shouldBe true
//            OMSRootPath.root() shouldBe expectedPath
//        }
//
//        test("should not recreate oms directory if it already exists") {
//            val existingPath = tempDir.resolve("oms")
//            existingPath.toFile().mkdirs()
//            existingPath.toFile().exists() shouldBe true
//
//            OMSRootPath.init(mockServer)
//
//            existingPath.toFile().exists() shouldBe true
//            OMSRootPath.root() shouldBe existingPath
//        }
//    }
//
//    context("stopCause") {
//        test("should resolve stop_cause json") {
//            OMSRootPath.init(mockServer)
//
//            val stopCausePath = OMSRootPath.stopCause()
//            val expectedStopCausePath = tempDir.resolve("oms").resolve("stop_cause.json")
//            stopCausePath shouldBe expectedStopCausePath
//        }
//    }
//
//    context("root") {
//        test("should throw if not initialized") {
//            shouldThrow<IllegalStateException> {
//                OMSRootPath.root()
//            }
//        }
//    }

})