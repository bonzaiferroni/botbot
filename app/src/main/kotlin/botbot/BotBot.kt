package botbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji

class BotBot(
    private val aiService: AiService,
    private var botConfig: BotConfig,
    private val selfId: Snowflake
) {
    private val conversations: MutableMap<Snowflake, Conversation> = mutableMapOf()

    suspend fun process(message: Message): BotResponse {
        val conversation = getConversation(message)
        return process(message, conversation)
    }

    private suspend fun process(message: Message, conversation: Conversation): BotResponse {
        if (!canHear(message)) return BotResponse(null, MessageTarget.Unknown)
        val target = findMessageTarget(message, conversation)
        if (target is MessageTarget.Unknown) return BotResponse(null, target)
        message.channel.type()
        val response = reply(message, conversation)
        if (response != null && response.isSingleEmoji()) {
            message.addReaction(ReactionEmoji.Unicode(response))
            return BotResponse(null, target)
        }
        return BotResponse(response, target)
    }

    private fun getConversation(message: Message): Conversation {
        val key = message.channelId
        var conversation = conversations[key] ?: Conversation(emptyList(), null)
        conversation = conversation.copy(messages = conversation.messages + message)
        conversations[key] = conversation
        return conversation
    }

    private fun canHear(message: Message): Boolean {
        if (message.author?.isBot != false) return false
        // if (message.isDM() && message.isBonzai()) return true

        val channelId = message.channel.id.value
        if (botConfig.safeChannels.contains(channelId)) return true
        if (message.mentionsUser(selfId) && message.content.contains("you can talk here", ignoreCase = true)) {
            botConfig = botConfig.copy(safeChannels = botConfig.safeChannels + channelId)
            botConfig.save()
            return true
        }
        return false
    }

    private fun findMessageTarget(message: Message, conversation: Conversation): MessageTarget {
        // if (message.isDM() && message.isBonzai()) return MessageTarget.Bot("DM from bonzai")
        if (message.mentionsUser("botbot", selfId)) return MessageTarget.Bot("Mentioned")
        if (conversation.messages.isRecentBackAndForth(selfId)) return MessageTarget.Bot("Back and forth")
        return MessageTarget.Unknown
    }

    private suspend fun reply(message: Message, conversation: Conversation): String? {
        val botContentResponse = aiService.prompt(message.content, conversation) ?: return null
        return botContentResponse.response
    }
}

data class BotResponse(
    val message: String?,
    val target: MessageTarget
)