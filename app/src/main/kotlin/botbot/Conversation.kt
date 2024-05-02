package botbot

import dev.kord.core.entity.Message

data class Conversation(
    val messages: List<Message>,
    val summary: String?,
)