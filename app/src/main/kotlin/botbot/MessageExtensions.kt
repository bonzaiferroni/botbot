package botbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

fun Message.isDM(): Boolean {
    return this.data.guildId.value == null
}

fun Message.isBonzai(): Boolean {
    return this.author?.username == ".bonzai"
}

fun Message.mentionsUser(name: String, id: Snowflake): Boolean {
    return this.content.startsWith(name, ignoreCase = true) ||
            this.mentionedUserIds.contains(id)
}

fun Message.mentionsUser(id: Snowflake) = this.mentionedUserIds.contains(id)

fun List<Message>.isRecentBackAndForth(id: Snowflake): Boolean {
    return this.size > 1 && this[this.size - 2].author?.id == id
            && this.secondsSinceLastMessage()!! < 30
}

fun List<Message>.secondsSinceLastMessage(): Int? {
    return if (this.isNotEmpty()) {
        (Clock.System.now() - this[this.size - 1].timestamp).inWholeSeconds.toInt()
    } else {
        null
    }
}