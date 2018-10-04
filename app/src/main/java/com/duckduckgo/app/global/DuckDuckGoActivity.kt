/*
 * Copyright (c) 2017 DuckDuckGo
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

package com.duckduckgo.app.global

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.app.statistics.VariantManager
import dagger.android.AndroidInjection
import javax.inject.Inject


abstract class DuckDuckGoActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var variantManager: VariantManager

    private var themeChangeReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        themeChangeReceiver = applyTheme(settingsDataStore, variantManager.getVariant())
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        themeChangeReceiver?.let {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(it)
        }
        super.onDestroy()
    }

    protected inline fun <reified V : ViewModel> bindViewModel() = lazy { ViewModelProviders.of(this, viewModelFactory).get(V::class.java) }
}
