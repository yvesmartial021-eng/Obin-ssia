package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OBIN’SS IA", appName)
  }

  @Test
  fun `verify ai tools catalog is populated`() {
    val tools = com.example.obinssia.data.model.AiToolsCatalog.allTools
    org.junit.Assert.assertTrue(tools.isNotEmpty())
    org.junit.Assert.assertTrue(tools.any { it.id == "tiktok_viral" })
    org.junit.Assert.assertTrue(tools.any { it.id == "code_generator" })
  }

  @Test
  fun `verify chat message creation`() {
    val message = ChatMessage(
        text = "Test prompt to Gemini",
        isFromUser = true
    )
    assertEquals("Test prompt to Gemini", message.text)
    org.junit.Assert.assertTrue(message.isFromUser)
    org.junit.Assert.assertFalse(message.isError)
  }
}
