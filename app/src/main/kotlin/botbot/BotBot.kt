package botbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message

class BotBot(
    private val aiService: AiService,
    private var botConfig: BotConfig,
    private val selfId: Snowflake
) {
    private val history: MutableMap<Snowflake, MutableList<Message>> = mutableMapOf()

    suspend fun process(message: Message): String? {
        if (!canHear(message)) return null
        val history = getHistory(message)
        if (!isForMe(message, history)) return null
        message.channel.type()
        return reply(message)
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

    private fun isForMe(message: Message, history: List<Message>): Boolean {
        if (message.isDM() && message.isBonzai()) return true
        if (message.mentionsUser("botbot", selfId)) return true
        if (history.isRecentBackAndForth(selfId)) return true
        return false
    }

    private suspend fun reply(message: Message): String? {
        return aiService.prompt(message.content)
    }
}