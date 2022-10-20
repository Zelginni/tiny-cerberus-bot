package ru.zelginni.tinycerberusbot.bot

data class CommandResult(
    val status: CommandStatus,
    val message: String? = null,
    val resultAction: ResultAction = ResultAction.Print
)

enum class CommandStatus { Success, Error }

enum class ResultAction { Print, Ban }