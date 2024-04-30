package botbot

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI

class AiService(token: String) {
    private val openAI = OpenAI(token, logging = LoggingConfig(LogLevel.None))
    private val history = mutableListOf<ChatMessage>()
    private val messageBase = listOf(
        ChatMessage(
            role = ChatRole.System,
            content = botInstructions
        )
    )

    suspend fun prompt(prompt: String): String? {
        history.add(ChatMessage(role = ChatRole.User, content = prompt))

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messageBase + history
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        val reply = completion.choices.first().message
        if (reply.content.isNullOrBlank()) return null
        history.add(reply)
        return completion.choices.first().message.content
    }
}