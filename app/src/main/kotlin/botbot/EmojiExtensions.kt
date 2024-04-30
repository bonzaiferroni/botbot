package botbot

import com.vdurmont.emoji.EmojiParser

fun String.isSingleEmoji(): Boolean {
    val emojis = EmojiParser.extractEmojis(this)
    return emojis?.size == 1 && this == emojis[0]
}