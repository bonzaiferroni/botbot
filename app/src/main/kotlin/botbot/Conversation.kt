package botbot

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val messages: List<MessageInfo>,
    val summary: String?,
)

@Serializable
data class MessageInfo(
    val content: String,
    val authorId: Snowflake,
    val timestamp: Instant,
)