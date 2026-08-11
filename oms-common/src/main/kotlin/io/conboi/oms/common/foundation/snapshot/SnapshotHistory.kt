package io.conboi.oms.common.foundation.snapshot

import io.conboi.oms.common.foundation.TimeHelper
import kotlin.math.ceil
import kotlin.time.Duration

abstract class SnapshotHistory<T : Snapshot, S : SnapshotHistory.Summary>(
    val retentionWindow: Duration,
    val checkInterval: Duration,
    val capacity: Int = ceil(
        retentionWindow.inWholeMilliseconds.toDouble() / checkInterval.inWholeMilliseconds
    ).toInt()
) {
    companion object {
        const val DEFAULT_AVERAGE_VALUE = 0.0
    }

    init {
        require(retentionWindow.isPositive()) { "countTime must be positive" }
        require(checkInterval.isPositive()) { "checkInterval must be positive" }
        require(capacity > 0) { "maxSize must be greater than 0" }
    }

    protected val history: ArrayDeque<T> = ArrayDeque<T>(capacity)

    fun add(snapshot: T) {
        if (history.size == capacity) {
            history.removeFirst()
        }
        history.addLast(snapshot)
    }

    fun latest(): T? {
        return history.lastOrNull()
    }

    fun averageOf(snapshotList: List<T> = history, selector: (T) -> Double): Double {
        if (snapshotList.isEmpty()) return DEFAULT_AVERAGE_VALUE

        var sum = 0.0
        for (snapshot in snapshotList) {
            sum += selector(snapshot)
        }

        return sum / snapshotList.size
    }

    open fun filteredHistoryOver(window: Duration): List<T> {
        require(window.isPositive()) { "window must be positive" }
        val cutoff = TimeHelper.currentEpochSeconds - window.inWholeSeconds
        return history.filter { it.createdAt >= cutoff }
    }

    abstract fun summary(): S

    abstract fun summaryOver(window: Duration): S


    interface Summary {
        val snapshotsCount: Int
    }
}
