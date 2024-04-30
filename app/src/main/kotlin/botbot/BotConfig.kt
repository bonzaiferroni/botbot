package botbot

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class BotConfig(
    val safeChannels: List<ULong>
) {

    fun save() {
        saveBotConfig(this)
    }

    companion object {
        private val path = "botconfig.json"
        private val json by lazy { Json { ignoreUnknownKeys = true } }

        fun load(): BotConfig {
            val file = File(path)
            if (!file.exists()) {
                return BotConfig(emptyList())
            }
            val text = file.readText()
            return json.decodeFromString<BotConfig>(text)
        }

        fun saveBotConfig(config: BotConfig) {
            val text = json.encodeToString(config)
            File(path).writeText(text)
        }
    }
}
