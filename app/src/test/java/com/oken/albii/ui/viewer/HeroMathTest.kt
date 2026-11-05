package com.oken.albii.ui.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeroMathTest {

    private val eps = 0.001f

    private fun assertRect(expected: Rect, actual: Rect) {
        assertEquals("left", expected.left, actual.left, eps)
        assertEquals("top", expected.top, actual.top, eps)
        assertEquals("right", expected.right, actual.right, eps)
        assertEquals("bottom", expected.bottom, actual.bottom, eps)
    }

    @Test
    fun fitRectLetterboxesWideImageInPortraitViewport() {
        // 1000x2000 の画面に 2:1 の横長画像 → 幅いっぱい・高さ 500、縦は中央
        val rect = fitRect(Size(1000f, 2000f), imageAspect = 2f)
        assertRect(Rect(0f, 750f, 1000f, 1250f), rect)
    }

    @Test
    fun fitRectPillarboxesTallImageInPortraitViewport() {
        // 1000x2000 の画面に 1:4 の縦長画像 → 高さいっぱい・幅 500、横は中央
        val rect = fitRect(Size(1000f, 2000f), imageAspect = 0.25f)
        assertRect(Rect(250f, 0f, 750f, 2000f), rect)
    }

    @Test
    fun fitRectOfMatchingAspectFillsViewport() {
        assertRect(Rect(0f, 0f, 1000f, 2000f), fitRect(Size(1000f, 2000f), imageAspect = 0.5f))
    }

    @Test
    fun fitRectFallsBackToViewportForInvalidInput() {
        assertRect(Rect(0f, 0f, 1000f, 2000f), fitRect(Size(1000f, 2000f), imageAspect = 0f))
        assertRect(Rect(0f, 0f, 0f, 0f), fitRect(Size.Zero, imageAspect = 1.5f))
    }

    @Test
    fun scaleAroundCenterKeepsCenterFixed() {
        val rect = Rect(100f, 200f, 300f, 600f)
        val scaled = scaleRectAround(rect, rect.center, 0.5f)
        assertEquals(rect.center.x, scaled.center.x, eps)
        assertEquals(rect.center.y, scaled.center.y, eps)
        assertEquals(100f, scaled.width, eps)
        assertEquals(200f, scaled.height, eps)
    }

    @Test
    fun scaleAroundPivotKeepsThePivotPointFixed() {
        val rect = Rect(0f, 0f, 1000f, 2000f)
        val pivot = Offset(300f, 800f)
        val scaled = scaleRectAround(rect, pivot, 0.5f)
        // pivot の画像上の相対位置（30%, 40%）は縮小後も同じ
        assertEquals(pivot.x, scaled.left + scaled.width * 0.3f, eps)
        assertEquals(pivot.y, scaled.top + scaled.height * 0.4f, eps)
    }

    @Test
    fun zoomedRectScalesAroundViewportCenterThenPans() {
        val fit = Rect(0f, 750f, 1000f, 1250f)
        val rect = zoomedRect(fit, viewportCenter = Offset(500f, 1000f), scale = 2f, pan = Offset(-40f, 30f))
        // 中心基準で 2 倍: 幅 2000・高さ 1000、中心は (500, 1000) のまま → パン分だけずれる
        assertRect(Rect(-540f, 530f, 1460f, 1530f), rect)
        assertEquals(2000f, rect.width, eps)
        assertEquals(1000f, rect.height, eps)
    }

    @Test
    fun dragProgressGrowsWithDownwardDragAndSaturates() {
        assertEquals(0f, dragProgress(0f, 2000f), eps)
        assertEquals(0.5f, dragProgress(500f, 2000f), eps)
        assertEquals(1f, dragProgress(1000f, 2000f), eps)
        assertEquals(1f, dragProgress(5000f, 2000f), eps)
    }

    @Test
    fun dragProgressIsZeroForUpwardDragOrUnknownViewport() {
        assertEquals(0f, dragProgress(-300f, 2000f), eps)
        assertEquals(0f, dragProgress(300f, 0f), eps)
    }

    @Test
    fun dragRectAtRestEqualsFit() {
        val fit = Rect(0f, 750f, 1000f, 1250f)
        assertRect(fit, dragRect(fit, Offset(500f, 1000f), Offset.Zero, 2000f))
    }

    @Test
    fun dragRectFollowsFingerAndShrinks() {
        val fit = Rect(0f, 0f, 1000f, 2000f)
        val pivot = Offset(500f, 1000f)
        val drag = Offset(60f, 500f)
        val rect = dragRect(fit, pivot, drag, viewportHeight = 2000f)

        // progress = 0.5 → scale = 0.75。pivot が画像中心なので、中心は pivot + drag に来る
        assertEquals(750f, rect.width, eps)
        assertEquals(1500f, rect.height, eps)
        assertEquals(pivot.x + drag.x, rect.center.x, eps)
        assertEquals(pivot.y + drag.y, rect.center.y, eps)
    }

    @Test
    fun dragRectMovesUpWithoutScalingWhenDraggingUp() {
        val fit = Rect(0f, 0f, 1000f, 2000f)
        val rect = dragRect(fit, Offset(500f, 1000f), Offset(0f, -200f), viewportHeight = 2000f)
        assertEquals(1000f, rect.width, eps)
        assertEquals(-200f, rect.top, eps)
    }

    @Test
    fun scrimFadesAsTheImageIsPulledDown() {
        assertEquals(1f, dragScrimAlpha(0f, 2000f), eps)
        assertEquals(0.5f, dragScrimAlpha(500f, 2000f), eps)
        assertEquals(0f, dragScrimAlpha(1000f, 2000f), eps)
        assertEquals(1f, dragScrimAlpha(-100f, 2000f), eps)
    }

    private val thresholds = DismissThresholds(distancePx = 300f, flingVelocityPx = 2000f, flingMinTravelPx = 60f)

    @Test
    fun dismissesWhenDraggedFarEnough() {
        assertTrue(shouldDismiss(dy = 300f, velocityY = 0f, thresholds = thresholds))
        assertTrue(shouldDismiss(dy = 900f, velocityY = -500f, thresholds = thresholds))
    }

    @Test
    fun doesNotDismissWhenShortAndSlow() {
        assertFalse(shouldDismiss(dy = 299f, velocityY = 100f, thresholds = thresholds))
    }

    @Test
    fun dismissesOnFastFlingAfterMinimalTravel() {
        assertTrue(shouldDismiss(dy = 80f, velocityY = 2500f, thresholds = thresholds))
        assertFalse(shouldDismiss(dy = 30f, velocityY = 2500f, thresholds = thresholds))
    }

    @Test
    fun neverDismissesUpward() {
        assertFalse(shouldDismiss(dy = -400f, velocityY = 5000f, thresholds = thresholds))
        assertFalse(shouldDismiss(dy = 0f, velocityY = 5000f, thresholds = thresholds))
    }

    @Test
    fun cornerRadiusIsZeroAtBothEndsAndPeaksInTheMiddle() {
        val origin = Rect(0f, 0f, 100f, 100f)
        val fit = Rect(0f, 0f, 1100f, 1100f)
        assertEquals(0f, heroCornerRadius(origin, origin, fit, 24f), eps)
        assertEquals(0f, heroCornerRadius(fit, origin, fit, 24f), eps)
        val middle = Rect(0f, 0f, 600f, 600f)
        assertEquals(24f, heroCornerRadius(middle, origin, fit, 24f), eps)
    }

    @Test
    fun cornerRadiusIsZeroWhenOriginAndFitHaveTheSameWidth() {
        val rect = Rect(0f, 0f, 500f, 500f)
        assertEquals(0f, heroCornerRadius(rect, rect, rect, 24f), eps)
    }
}
