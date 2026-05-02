package ru.zelginni.tinycerberusbot.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
class SwaggerConfig(
    private val buildProperties: ObjectProvider<BuildProperties>,
) {
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("Tiny Cerberus Bot")
                    .description("Discipline Telegram Chat bot")
                    .version(applicationVersion())
            )
    }

    private fun applicationVersion(): String =
        buildProperties.ifAvailable
            ?.version
            ?.let { "v$it" }
            ?: "unknown"
}
