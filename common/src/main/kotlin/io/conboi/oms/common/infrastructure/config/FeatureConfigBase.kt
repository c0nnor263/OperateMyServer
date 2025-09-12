package io.conboi.oms.common.infrastructure.config

abstract class FeatureConfigBase : ConfigBase() {
    val enabled = b(true, "enabled")
}