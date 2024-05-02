package botbot

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class JsonMemory(
    val safeChannels: List<ULong>,
    val conversations: MutableMap<Snowflake, Conversation> = mutableMapOf()
) {

    fun save() {
        saveBotConfig(this)
    }

    companion object {
        private val path = "memory.json"
        private val json by lazy { Json { ignoreUnknownKeys = true } }

        fun load(): JsonMemory {
            val file = File(path)
            if (!file.exists()) {
                return JsonMemory(emptyList())
            }
            val text = file.readText()
            return json.decodeFromString<JsonMemory>(text)
        }

        fun saveBotConfig(config: JsonMemory) {
            val text = json.encodeToString(config)
            File(path).writeText(text)
        }
    }
}
