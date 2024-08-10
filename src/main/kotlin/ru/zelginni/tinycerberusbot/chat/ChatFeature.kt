package ru.zelginni.tinycerberusbot.chat

enum class ChatFeature {
    BAYAN {
        override fun enable(chat: Chat) {
            chat.bayanEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.bayanEnabled = false
        }
    },
    DIGEST {
        override fun enable(chat: Chat) {
            chat.digestEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.digestEnabled = false
        }
    },
    RULES {
        override fun enable(chat: Chat) {
            chat.rulesEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.rulesEnabled = false
        }
    };

    abstract fun enable(chat: Chat)
    abstract fun disable(chat: Chat)
}

fun getFeature(name: String): ChatFeature? = ChatFeature.values().firstOrNull{ it.name.equals(name, true) }