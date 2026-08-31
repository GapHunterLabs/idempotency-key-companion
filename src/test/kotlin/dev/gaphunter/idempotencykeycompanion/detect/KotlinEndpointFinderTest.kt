package dev.gaphunter.idempotencykeycompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.idempotencykeycompanion.model.EndpointMethodKind

class KotlinEndpointFinderTest : BasePlatformTestCase() {

    fun `test a PostMapping with no RequestHeader param is flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @PostMapping("/orders")
                fun createOrder(@RequestBody order: Order) { }
            }
            """.trimIndent(),
        )
        val hits = KotlinEndpointFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(EndpointMethodKind.POST, hits[0].kind)
    }

    fun `test a PostMapping with an Idempotency-Key RequestHeader is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @PostMapping("/orders")
                fun createOrder(@RequestHeader("Idempotency-Key") key: String, @RequestBody order: Order) { }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinEndpointFinder.findAll(file).isEmpty())
    }

    fun `test a PostMapping with an Idempotence-Key RequestHeader (alternate spelling) is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @PostMapping("/orders")
                fun createOrder(@RequestHeader("Idempotence-Key") key: String, @RequestBody order: Order) { }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinEndpointFinder.findAll(file).isEmpty())
    }

    fun `test PutMapping is also checked`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @PutMapping("/orders/{id}")
                fun updateOrder(@RequestBody order: Order) { }
            }
            """.trimIndent(),
        )
        val hits = KotlinEndpointFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(EndpointMethodKind.PUT, hits[0].kind)
    }

    fun `test a GET endpoint is never flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @GetMapping("/orders")
                fun listOrders() { }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinEndpointFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated RequestHeader does not clear the warning`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                @PostMapping("/orders")
                fun createOrder(@RequestHeader("Authorization") auth: String, @RequestBody order: Order) { }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinEndpointFinder.findAll(file).size)
    }

    fun `test a plain function with no mapping annotation is never flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun helper() { }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinEndpointFinder.findAll(file).isEmpty())
    }
}
