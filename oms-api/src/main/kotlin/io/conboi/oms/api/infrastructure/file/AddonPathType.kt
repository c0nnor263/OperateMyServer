package io.conboi.oms.api.infrastructure.file

/**
 * Defines logical categories of filesystem paths assigned to an OMS addon.
 *
 * Each [AddonPathType] represents a distinct purpose and is used
 * by OMS to provide structured, policy-controlled access to the
 * filesystem for addons.
 */
enum class AddonPathType {

    /**
     * Common-purpose addon directory.
     *
     * Typically used for persistent addon data that does not fit
     * into more specialized categories.
     */
    COMMON,

    /**
     * Directory intended for addon-generated logs.
     */
    LOGS,

    /**
     * Directory intended for cache and temporary data.
     *
     * Data stored here may be cleared or regenerated at any time.
     */
    CACHE
}
