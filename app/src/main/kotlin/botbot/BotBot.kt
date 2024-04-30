package botbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import kotlinx.datetime.Clock

class BotBot(
    private val aiService: AiService,
    private var botConfig: BotConfig,
    private val selfId: Snowflake
) {
    private val history: MutableMap<Snowflake, MutableList<Message>> = mutableMapOf()

    suspend fun process(message: Message): BotResponse {
        if (!canHear(message)) return BotResponse(null, MessageTarget.Unknown)
        val history = getHistory(message)
        val target = findMessageTarget(message, history)
        if (target is MessageTarget.Unknown) BotResponse(null, target)
        message.channel.type()
        val response = reply(message)
        if (response != null && response.isSingleEmoji()) {
            message.addReaction(ReactionEmoji.Unicode(response))
            message.channel.type()
            return BotResponse(null, target)
        }
        return BotResponse(response, target)
    }

    private fun getHistory(message: Message): List<Message> {
        val key = message.channelId
        if (!history.contains(key)) {
            history[key] = mutableListOf()
        }
        val list = history[key]?.toList() ?: emptyList()
        history[key]?.add(message)
        return list
    }

    private fun canHear(message: Message): Boolean {
        if (message.author?.isBot != false) return false
        if (message.isDM() && message.isBonzai()) return true

        val channelId = message.channel.id.value
        if (botConfig.safeChannels.contains(channelId)) return true
        if (message.mentionsUser(selfId) && message.content.contains("you can talk here", ignoreCase = true)) {
            botConfig = botConfig.copy(safeChannels = botConfig.safeChannels + channelId)
            botConfig.save()
            return true
        }
        return false
    }

    private fun findMessageTarget(message: Message, history: List<Message>): MessageTarget {
        if (message.isDM() && message.isBonzai()) return MessageTarget.Bot("DM from bonzai")
        if (message.mentionsUser(selfId)) return MessageTarget.Bot("Mentioned")
        if (history.isRecentBackAndForth(selfId)) return MessageTarget.Bot("Back and forth")
        return MessageTarget.Unknown
    }

    private suspend fun reply(message: Message): String? {
        return aiService.prompt(message.content)
    }
}

data class BotResponse(
    val message: String?,
    val target: MessageTarget
)

sealed class MessageTarget {
    data class Bot(val reason: String) : MessageTarget() {
        override fun toString(): String {
            return "Bot($reason)"
        }
    }
    data object Unknown : MessageTarget()
}