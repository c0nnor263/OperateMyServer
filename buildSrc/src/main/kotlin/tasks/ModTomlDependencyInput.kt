package tasks

import java.io.Serializable

data class ModTomlDependencyInput(
    val modId: String,
    val mandatory: Boolean,
    val versionRange: String,
    val ordering: String,
    val side: String
) : Serializable