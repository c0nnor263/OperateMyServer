abstract class Info {
    abstract val ID: String
    abstract val VERSION: String
    abstract val DISPLAY_NAME: String
    open val MOD_AUTHORS: String = "c0nnor263"
    abstract val DESCRIPTION: String
    open val MOD_LICENSE: String = "MIT"
    abstract val GROUP_ID: String
}