package ru.zelginni.tinycerberusbot.chat

enum class ChatFeature(val displayName: String) {
    BAYAN("Реакция на баяны") {
        override fun enable(chat: Chat) {
            chat.bayanEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.bayanEnabled = false
        }

        override fun isEnabled(chat: Chat): Boolean = chat.bayanEnabled == true
    },
    DIGEST("Дайджест") {
        override fun enable(chat: Chat) {
            chat.digestEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.digestEnabled = false
        }

        override fun isEnabled(chat: Chat): Boolean = chat.digestEnabled == true
    },
    RULES("Правила чата") {
        override fun enable(chat: Chat) {
            chat.rulesEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.rulesEnabled = false
        }

        override fun isEnabled(chat: Chat): Boolean = chat.rulesEnabled == true
    },
    STATISTICS("Статистика") {
        override fun enable(chat: Chat) {
            chat.statisticsEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.statisticsEnabled = false
        }

        override fun isEnabled(chat: Chat): Boolean = chat.statisticsEnabled == true
    },
    PYTHIA("Предсказания") {
        override fun enable(chat: Chat) {
            chat.pythiaEnabled = true
        }

        override fun disable(chat: Chat) {
            chat.pythiaEnabled = false
        }

        override fun isEnabled(chat: Chat): Boolean = chat.pythiaEnabled == true
    };

    abstract fun enable(chat: Chat)
    abstract fun disable(chat: Chat)
    abstract fun isEnabled(chat: Chat): Boolean
}

fun getFeature(name: String): ChatFeature? = ChatFeature.values().firstOrNull{ it.name.equals(name, true) }
