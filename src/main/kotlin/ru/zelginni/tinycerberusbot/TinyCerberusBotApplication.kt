package ru.zelginni.tinycerberusbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TinyCerberusBotApplication

fun main(args: Array<String>) {
	runApplication<TinyCerberusBotApplication>(*args)
}
