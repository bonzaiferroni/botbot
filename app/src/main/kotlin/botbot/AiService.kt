package botbot

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class AiService(token: String) {
    private val openAI = OpenAI(token, logging = LoggingConfig(LogLevel.None))
    private val history = mutableListOf<ChatMessage>()
    private val messageBase = listOf(
        ChatMessage(
            role = ChatRole.System,
            content = botInstructions
        )
    )

    suspend fun prompt(prompt: String, conversation: Conversation): AiResponse? {
        val promptText = getPromptText(prompt, conversation.summary)
        history.add(ChatMessage(role = ChatRole.User, content = promptText))
        if (history.size > 20) {
            history.removeAt(0)
            history.removeAt(0)
        }

        val chatCompletionRequest = chatCompletionRequest {
            model = ModelId("gpt-3.5-turbo-0125")
            messages = messageBase + history
            toolChoice = ToolChoice.function("botbot")
            tools {
                function(
                    name = "botbot",
                    description = "understand and respond to discord chat",
                ) {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("mood") {
                            put("type", "string")
                            put("description", "the mood of the user")
                        }
                        putJsonObject("isQuestion") {
                            put("type", "boolean")
                            put("description", "whether the user is requesting for information")
                        }
                        putJsonObject("response") {
                            put("type", "string")
                            put("description", "the response to the user")
                        }
                    }
                    putJsonArray("required") {
                        add("isQuestion")
                        add("response")
                    }
                }
            }
        }

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        val content = getMessage(completion.choices.first().message) ?: return null
        history.add(ChatMessage(role = ChatRole.Assistant, content = content.response))
        return content
    }

    fun getSummary(conversation: Conversation): String {
        val chatCompletionRequest = chatCompletionRequest {
            model = ModelId("gpt-3.5-turbo-0125")
            messages = listOf(
                ChatMessage(role = ChatRole.System, content = summarizeInstructions),
                ChatMessage(role = ChatRole.User, content = conversation.summary)
            )
        }
    }

    private fun getMessage(message: ChatMessage?): AiResponse? {
        if (message == null) return null
        val toolCall = message.toolCalls?.first()
        if (toolCall !is ToolCall.Function && message.content != null) {
            return AiResponse(message.content)
        }
        require(toolCall is ToolCall.Function)
        val functionCall = toolCall.function
        val text = functionCall.argumentsOrNull ?: return null
        println(text)
        return json.decodeFromString<AiResponse>(text)
    }
}

val json by lazy { Json { ignoreUnknownKeys = true } }

@Serializable
data class AiResponse(
    val response: String?,
    val mood: String? = null,
    val isQuestion: Boolean? = null,
)