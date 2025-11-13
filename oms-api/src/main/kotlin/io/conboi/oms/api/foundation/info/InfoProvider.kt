package io.conboi.oms.api.foundation.info

interface InfoProvider<out T : OmsInfo> {
    fun info(): T
}