package com.oken.albii.ui.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.lerp
import kotlin.math.abs

/** 閉じるドラッグで画像が縮む下限の倍率（画面高さの半分ぶん動かしたとき）。 */
private const val MIN_DRAG_SCALE = 0.5f

/** 縮みと背景の薄まりが最大になるまでのドラッグ距離（画面高さに対する割合）。 */
private const val FULL_DRAG_FRACTION = 0.5f

/** [imageAspect]（幅 / 高さ）の画像を [viewport] の中央に Fit で収めたときの矩形。 */
fun fitRect(viewport: Size, imageAspect: Float): Rect {
    if (viewport.width <= 0f || viewport.height <= 0f || imageAspect <= 0f) {
        return Rect(Offset.Zero, viewport)
    }
    val viewportAspect = viewport.width / viewport.height
    val size = if (imageAspect >= viewportAspect) {
        Size(viewport.width, viewport.width / imageAspect)
    } else {
        Size(viewport.height * imageAspect, viewport.height)
    }
    return Rect(
        Offset((viewport.width - size.width) / 2f, (viewport.height - size.height) / 2f),
        size,
    )
}

/** [rect] を、点 [pivot] を基準に [scale] 倍した矩形。 */
fun scaleRectAround(rect: Rect, pivot: Offset, scale: Float): Rect = Rect(
    left = pivot.x + (rect.left - pivot.x) * scale,
    top = pivot.y + (rect.top - pivot.y) * scale,
    right = pivot.x + (rect.right - pivot.x) * scale,
    bottom = pivot.y + (rect.bottom - pivot.y) * scale,
)

/** ズーム中の画像の表示位置。ビューポート中心を基準に [scale] 倍し、パン量 [pan] だけ動かす。 */
fun zoomedRect(fit: Rect, viewportCenter: Offset, scale: Float, pan: Offset): Rect =
    scaleRectAround(fit, viewportCenter, scale).translate(pan)

/** 閉じるドラッグの進み具合（0〜1）。下方向の移動量が画面高さの半分に達すると 1。上方向は 0。 */
fun dragProgress(dy: Float, viewportHeight: Float): Float {
    if (viewportHeight <= 0f) return 0f
    return (dy / (viewportHeight * FULL_DRAG_FRACTION)).coerceIn(0f, 1f)
}

/**
 * ドラッグ中のヒーロー矩形。触り始めた点 [pivot] を基準に縮め、指の移動 [drag] に追従させる。
 * 基準を指の位置にすることで、触った画像上の点が指の下に残る。
 */
fun dragRect(fit: Rect, pivot: Offset, drag: Offset, viewportHeight: Float): Rect {
    val scale = lerp(1f, MIN_DRAG_SCALE, dragProgress(drag.y, viewportHeight))
    return scaleRectAround(fit, pivot, scale).translate(drag)
}

/** ドラッグ中の背景（黒）の濃さ。引っ張るほど薄くなり、下の一覧が見えてくる。 */
fun dragScrimAlpha(dy: Float, viewportHeight: Float): Float = 1f - dragProgress(dy, viewportHeight)

/** 指を離したときに閉じるかどうかを決める距離・速度のしきい値（px、px/秒）。 */
class DismissThresholds(
    val distancePx: Float,
    val flingVelocityPx: Float,
    val flingMinTravelPx: Float,
)

/** 十分な距離を引いたか、少しでも勢いよく払ったときに閉じる。上方向には閉じない。 */
fun shouldDismiss(dy: Float, velocityY: Float, thresholds: DismissThresholds): Boolean =
    dy > 0f && (dy >= thresholds.distancePx || (velocityY >= thresholds.flingVelocityPx && dy >= thresholds.flingMinTravelPx))

/**
 * 開閉の途中で付ける角丸の半径。サムネイル（[origin]）と全画面（[fit]）の両端では 0、中間で最大になる。
 * ドラッグ中も同じ式を使うので、指を離した瞬間に角丸が飛ばない。
 */
fun heroCornerRadius(frame: Rect, origin: Rect, fit: Rect, maxRadius: Float): Float {
    val span = fit.width - origin.width
    if (abs(span) < 1f) return 0f
    val expansion = ((frame.width - origin.width) / span).coerceIn(0f, 1f)
    return maxRadius * 4f * expansion * (1f - expansion)
}
