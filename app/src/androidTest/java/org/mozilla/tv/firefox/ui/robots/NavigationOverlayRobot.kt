/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.ui.robots

import android.net.Uri
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.clearText
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.mozilla.tv.firefox.R
import org.mozilla.tv.firefox.helpers.ext.assertIsEnabled
import org.mozilla.tv.firefox.helpers.ext.click
import org.mozilla.tv.firefox.pinnedtile.TileViewHolder

/**
 * Implementation of Robot Pattern for the navigation overlay menu.
 */
class NavigationOverlayRobot {

    fun goBack() = backButton().click()
    fun goForward() = forwardButton().click()
    fun reload() = reloadButton().click()
    fun toggleTurbo() = turboButton().click()
    fun openSettings() = settingsButton().click()

    fun assertCanGoBack(canGoBack: Boolean) = backButton().assertIsEnabled(canGoBack)
    fun assertCanGoForward(canGoForward: Boolean) = forwardButton().assertIsEnabled(canGoForward)
    fun assertCanGoBackForward(canGoBack: Boolean, canGoForward: Boolean) {
        assertCanGoBack(canGoBack)
        assertCanGoForward(canGoForward)
    }

    class Transition {

        fun enterUrlAndEnterToBrowser(url: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            urlBar().perform(clearText(),
                    typeText(url.toString()),
                    pressImeActionButton())

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openTileToBrowser(index: Int) {
            homeTiles().perform(RecyclerViewActions.actionOnItemAtPosition<TileViewHolder>(index, click()))
        }

        fun openTileToBrowser(title: String) {
            homeTiles().perform(RecyclerViewActions.actionOnItem<TileViewHolder>(hasDescendant(withText(title)), click()))
        }

        fun openSettings(interact: SettingsRobot.() -> Unit): SettingsRobot.Transition {
            settingsButton().click()

            SettingsRobot().interact()
            return SettingsRobot.Transition()
        }
    }
}

/**
 * Applies [interact] to a new [NavigationOverlayRobot]
 *
 * @sample org.mozilla.tv.firefox.session.ClearSessionTest.WHEN_data_is_cleared_THEN_back_and_forward_should_be_unavailable
 */
fun navigationOverlay(interact: NavigationOverlayRobot.() -> Unit): NavigationOverlayRobot.Transition {
    NavigationOverlayRobot().interact()
    return NavigationOverlayRobot.Transition()
}

private fun backButton() = onView(withId(R.id.navButtonBack))
private fun forwardButton() = onView(withId(R.id.navButtonForward))
private fun reloadButton() = onView(withId(R.id.navButtonReload))
private fun pinButton() = onView(withId(R.id.pinButton))
private fun turboButton() = onView(withId(R.id.turboButton))
private fun settingsButton() = onView(withId(R.id.navButtonSettings))
private fun urlBar() = onView(withId(R.id.navUrlInput))
private fun homeTiles() = onView(withId(R.id.tileContainer))
private fun overlay() = onView(withId(R.id.browserOverlay))