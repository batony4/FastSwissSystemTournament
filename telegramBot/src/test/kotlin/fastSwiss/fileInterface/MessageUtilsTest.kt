/*
 * This Kotlin source file was generated by the Gradle "init" task.
 */
package fastSwiss.fileInterface

import fastSwiss.telegramBot.MessageUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MessageUtilsTest {
    @Test
    fun testGetMessage() {
        assertEquals("Hello      World!", MessageUtils.getMessage())
    }
}
