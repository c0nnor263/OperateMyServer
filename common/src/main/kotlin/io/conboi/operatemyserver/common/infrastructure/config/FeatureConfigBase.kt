package io.conboi.operatemyserver.common.infrastructure.config

abstract class FeatureConfigBase : ConfigBase() {
    val enabled = b(true, "enabled")
}