package dev.gaphunter.idempotencykeycompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.idempotencykeycompanion.model.EndpointMethodKind

class JavaEndpointFinderTest : BasePlatformTestCase() {

    fun `test a PostMapping with no RequestHeader param is flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @PostMapping("/orders")
                void createOrder(@RequestBody Order order) { }
            }
            """.trimIndent(),
        )
        val hits = JavaEndpointFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(EndpointMethodKind.POST, hits[0].kind)
    }

    fun `test a PostMapping with an Idempotency-Key RequestHeader is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @PostMapping("/orders")
                void createOrder(@RequestHeader("Idempotency-Key") String key, @RequestBody Order order) { }
            }
            """.trimIndent(),
        )
        assertTrue(JavaEndpointFinder.findAll(file).isEmpty())
    }

    fun `test PutMapping is also checked`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @PutMapping("/orders/{id}")
                void updateOrder(@RequestBody Order order) { }
            }
            """.trimIndent(),
        )
        val hits = JavaEndpointFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(EndpointMethodKind.PUT, hits[0].kind)
    }

    fun `test RequestMapping with method equals RequestMethod-POST is recognized`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @RequestMapping(value = "/orders", method = RequestMethod.POST)
                void createOrder(@RequestBody Order order) { }
            }
            """.trimIndent(),
        )
        val hits = JavaEndpointFinder.findAll(file)
        assertEquals(1, hits.size)
    }

    fun `test a GET endpoint is never flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @GetMapping("/orders")
                void listOrders() { }
            }
            """.trimIndent(),
        )
        assertTrue(JavaEndpointFinder.findAll(file).isEmpty())
    }

    fun `test a RequestHeader parameter with a name attribute is also recognized`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @PostMapping("/orders")
                void createOrder(@RequestHeader(name = "X-Idempotency-Key") String key) { }
            }
            """.trimIndent(),
        )
        assertTrue(JavaEndpointFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated RequestHeader does not clear the warning`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                @PostMapping("/orders")
                void createOrder(@RequestHeader("Authorization") String auth, @RequestBody Order order) { }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaEndpointFinder.findAll(file).size)
    }

    fun `test a plain method with no mapping annotation is never flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void helper() { }
            }
            """.trimIndent(),
        )
        assertTrue(JavaEndpointFinder.findAll(file).isEmpty())
    }
}
