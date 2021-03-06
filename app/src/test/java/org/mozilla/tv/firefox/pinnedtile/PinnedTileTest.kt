/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.pinnedtile

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.tv.firefox.focus.FocusRepo
import org.mozilla.tv.firefox.navigationoverlay.NavigationOverlayViewModel
import org.mozilla.tv.firefox.navigationoverlay.channels.ChannelDetails
import org.mozilla.tv.firefox.session.SessionRepo
import org.mozilla.tv.firefox.utils.FormattedDomainWrapper
import org.robolectric.RobolectricTestRunner

const val DEFAULT_PINNED_TILE_COUNT = 10

/**
 * Unit tests for pinned tile operations in [NavigationOverlayViewModel].
 */
@RunWith(RobolectricTestRunner::class)
class PinnedTileTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    @MockK private lateinit var sessionRepo: SessionRepo
    @MockK private lateinit var focusRepo: FocusRepo
    @MockK private lateinit var pinnedTileImageUtilWrapper: PinnedTileImageUtilWrapper
    @MockK private lateinit var formattedDomainWrapper: FormattedDomainWrapper
    @MockK private lateinit var drawable: Drawable
    private lateinit var overlayVm: NavigationOverlayViewModel
    private lateinit var pinnedTileRepo: PinnedTileRepo
    private lateinit var testObserver: TestObserver<ChannelDetails>

    @Suppress("DEPRECATION")
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { focusRepo.focusUpdate } answers { Observable.empty() }
        every { focusRepo.defaultViewAfterScreenChange } answers { Observable.empty() }
        every { sessionRepo.legacyState } answers { MutableLiveData() }
        every { pinnedTileImageUtilWrapper.generatePinnedTilePlaceholder(any()) } answers { drawable }
        every { formattedDomainWrapper.format(any(), any(), any()) } answers { "" }

        pinnedTileRepo = PinnedTileRepo(ApplicationProvider.getApplicationContext())
        overlayVm = NavigationOverlayViewModel(
                sessionRepo,
                focusRepo,
                pinnedTileImageUtilWrapper,
                formattedDomainWrapper,
                "Pinned Tiles",
                pinnedTileRepo
        )
        testObserver = overlayVm.pinnedTiles.test()
    }

    @Test
    fun `WHEN repo emits a successful load THEN view model should emit a list of same number of tiles`() {
        assertEquals(1, testObserver.valueCount())
        assertEquals(DEFAULT_PINNED_TILE_COUNT, testObserver.values().last().tileList.size)
    }

    @Test
    fun `WHEN repo emits an updated list after add THEN view model should emit an updated list`() {
        assertEquals(DEFAULT_PINNED_TILE_COUNT, testObserver.values().last().tileList.size)
        pinnedTileRepo.addPinnedTile("https://example.com", null)
        assertEquals(2, testObserver.valueCount())
        assertEquals(DEFAULT_PINNED_TILE_COUNT + 1, testObserver.values().last().tileList.size)
    }

    @Test
    fun `WHEN repo emits an updated list after remove THEN view model should emit an updated list`() {
        assertEquals(DEFAULT_PINNED_TILE_COUNT, testObserver.values().last().tileList.size)
        overlayVm.unpinPinnedTile("https://www.instagram.com/")
        assertEquals(2, testObserver.valueCount())
        assertEquals(DEFAULT_PINNED_TILE_COUNT - 1, testObserver.values().last().tileList.size)
    }

    @Test
    fun `WHEN repo fails to remove an item THEN view model should emit nothing`() {
        assertEquals(DEFAULT_PINNED_TILE_COUNT, testObserver.values().last().tileList.size)
        overlayVm.unpinPinnedTile("https://example.com/")
        assertEquals(1, testObserver.valueCount())
        assertEquals(DEFAULT_PINNED_TILE_COUNT, testObserver.values().last().tileList.size)
    }
}
