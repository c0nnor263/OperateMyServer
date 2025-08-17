package io.conboi.restartmyserver.infrastructure.config

abstract class FeatureConfigBase : ConfigBase() {
    abstract val featureName: String

    override val name: String = "${featureName}_config"

    val enabled = b(true, "enabled")
}