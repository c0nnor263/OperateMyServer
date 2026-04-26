abstract class Info {
    abstract val ID: String
    abstract val VERSION: String
    abstract val DISPLAY_NAME: String
    open val AUTHOR: String = "c0nnor263"
    abstract val DESCRIPTION: String
    open val LICENSE: String = "MIT"
    abstract val GROUP_ID: String
}