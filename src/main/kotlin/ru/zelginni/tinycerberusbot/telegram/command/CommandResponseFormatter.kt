package ru.zelginni.tinycerberusbot.telegram.command

object CommandResponseFormatter {
    fun <T> formatNumberedStatistics(
        title: String,
        items: List<T>,
        emptyText: String,
        displayName: (T) -> String,
        messageCount: (T) -> Long,
    ): String =
        if (items.isEmpty()) {
            emptyText
        } else {
            items.mapIndexed { index, item ->
                "${index + 1}. ${displayName(item)} — ${messageCount(item)}"
            }.joinToString(
                separator = "\n",
                prefix = "$title\n",
            )
        }
}
