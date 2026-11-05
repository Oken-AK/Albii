package com.oken.albii.ui.viewer

/**
 * スクラブ（シークバーのドラッグ）中の seekTo を間引く。
 * デコーダーが 1 回のシークに追いつく前に次のシークで上書きすると、映像がいつまでも更新されなくなる。
 * そこで最小間隔を空けつつ、保留するのは常に最新の位置だけにする（途中の位置は捨てる）。
 * 時刻は呼び出し側から渡す（単体テストで時間を進められるようにするため）。
 */
class ScrubSeekThrottle(private val minIntervalMs: Long) {
    private var pendingMs: Long? = null
    private var lastSentAtMs: Long? = null

    /** 新しい目標位置を受け取る。すぐ送ってよければその位置を返し、間引くなら null（最新として保留する）。 */
    fun submit(positionMs: Long, nowMs: Long): Long? {
        pendingMs = positionMs
        return takeIfDue(nowMs)
    }

    /** 保留中の位置を、送ってよい時刻になっていれば返して保留を解く。 */
    fun takeIfDue(nowMs: Long): Long? {
        val pending = pendingMs ?: return null
        val last = lastSentAtMs
        if (last != null && nowMs - last < minIntervalMs) return null
        pendingMs = null
        lastSentAtMs = nowMs
        return pending
    }

    /** 保留中の位置が送れるようになるまでの待ち時間（ms）。保留が無ければ null。 */
    fun delayUntilDueMs(nowMs: Long): Long? {
        if (pendingMs == null) return null
        val last = lastSentAtMs ?: return 0L
        return (minIntervalMs - (nowMs - last)).coerceAtLeast(0L)
    }

    fun reset() {
        pendingMs = null
        lastSentAtMs = null
    }
}

/**
 * シークバーの位置（0〜1）を再生位置（ms）に変換する。
 * 終端ちょうどを指すと最終フレームが出ないことがあるため、1 ms 手前までに収める。
 */
fun scrubTargetMs(fraction: Float, durationMs: Long): Long {
    if (durationMs <= 0L) return 0L
    val target = (fraction.coerceIn(0f, 1f) * durationMs).toLong()
    return target.coerceIn(0L, durationMs - 1)
}
