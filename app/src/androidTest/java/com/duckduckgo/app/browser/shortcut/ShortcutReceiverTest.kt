/*
 * Copyright (c) 2020 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.browser.shortcut

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.app.cta.ui.UseOurAppCta
import com.duckduckgo.app.global.timestamps.db.KeyTimestampStore
import com.duckduckgo.app.runBlocking
import com.duckduckgo.app.statistics.pixels.Pixel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ShortcutReceiverTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val mockKeyTimestampStore: KeyTimestampStore = mock()
    private val mockPixel: Pixel = mock()
    private lateinit var testee: ShortcutReceiver
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun before() {

        testee = ShortcutReceiver(mockKeyTimestampStore, coroutinesTestRule.testDispatcherProvider, mockPixel)
    }

    @Test
    fun whenIntentReceivedIfUrlIsFromUseOurAppUrlThenRegisterTimestamp() = coroutinesTestRule.runBlocking {
        val intent = Intent()
        intent.putExtra(ShortcutBuilder.SHORTCUT_URL_ARG, UseOurAppCta.USE_OUR_APP_SHORTCUT_URL)
        intent.putExtra(ShortcutBuilder.SHORTCUT_TITLE_ARG, "Title")
        testee.onReceive(context, intent)

        verify(mockKeyTimestampStore).registerTimestamp(any())
    }

    @Test
    fun whenIntentReceivedIfUrlIsFromUseOurAppUrlThenFirePixel() {
        val intent = Intent()
        intent.putExtra(ShortcutBuilder.SHORTCUT_URL_ARG, UseOurAppCta.USE_OUR_APP_SHORTCUT_URL)
        intent.putExtra(ShortcutBuilder.SHORTCUT_TITLE_ARG, "Title")
        testee.onReceive(context, intent)

        verify(mockPixel).fire(Pixel.PixelName.USE_OUR_APP_SHORTCUT_ADDED)
    }

    @Test
    fun whenIntentReceivedIfUrlIsNotFromUseOurAppUrlThenDoNotRegisterTimestamp() = coroutinesTestRule.runBlocking {
        val intent = Intent()
        intent.putExtra(ShortcutBuilder.SHORTCUT_URL_ARG, "www.example.com")
        intent.putExtra(ShortcutBuilder.SHORTCUT_TITLE_ARG, "Title")
        testee.onReceive(context, intent)

        verify(mockKeyTimestampStore, never()).registerTimestamp(any())
    }

    @Test
    fun whenIntentReceivedIfUrlIsNotFromUseOurAppUrlThenDoNotFirePixel() {
        val intent = Intent()
        intent.putExtra(ShortcutBuilder.SHORTCUT_URL_ARG, "www.example.com")
        intent.putExtra(ShortcutBuilder.SHORTCUT_TITLE_ARG, "Title")
        testee.onReceive(context, intent)

        verify(mockPixel, never()).fire(Pixel.PixelName.USE_OUR_APP_SHORTCUT_ADDED)
    }
}
