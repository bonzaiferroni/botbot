package botbot

sealed class MessageTarget {
    data class Bot(val reason: String) : MessageTarget() {
        override fun toString(): String {
            return "Bot($reason)"
        }
    }
    data object Unknown : MessageTarget()
    data class OtherChannel(val channelName: String) : MessageTarget() {
        override fun toString(): String {
            return "OtherChannel($channelName)"
        }
    }
}