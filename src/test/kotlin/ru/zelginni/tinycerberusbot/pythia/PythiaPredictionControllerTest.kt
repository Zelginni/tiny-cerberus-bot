package ru.zelginni.tinycerberusbot.pythia

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PythiaPredictionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var repository: PythiaPredictionRepository

    @Test
    @WithMockUser
    fun addGetAndDeletePrediction() {
        repository.deleteAll()
        val text = "Сегодня звезды особенно благосклонны к рефакторингу."

        val addResult = mockMvc.post("/admin/bot/predictions") {
            content = objectMapper.writeValueAsString(AddPythiaPredictionRequest(text = text))
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect { status().isOk }
            .andExpect { jsonPath("$.text") { value(text) } }
            .andReturn()

        val prediction = objectMapper.readValue(addResult.response.contentAsString, PythiaPredictionView::class.java)
        assertEquals(text, repository.findById(prediction.id).orElseThrow().text)

        mockMvc.get("/admin/bot/predictions")
            .andDo { print() }
            .andExpect { status().isOk }
            .andExpect { jsonPath("$[0].id") { value(prediction.id) } }
            .andExpect { jsonPath("$[0].text") { value(text) } }

        mockMvc.delete("/admin/bot/predictions/${prediction.id}")
            .andDo { print() }
            .andExpect { status().isOk }

        assertFalse(repository.existsById(prediction.id))
    }
}
