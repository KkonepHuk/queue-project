package ru.without_title.queue_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.without_title.queue_project.database.entities.*;
import ru.without_title.queue_project.services.*;
import ru.without_title.queue_project.controllers.*;
import ru.without_title.queue_project.dto.request.*;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueService queueService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateQueue() throws Exception {
        UUID groupId = UUID.randomUUID();
        // Твой record QueueRequest
        QueueRequest request = new QueueRequest(
                "Зачет по БД",
                "Очередь в 405 кабинет",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(5),
                30,
                false);

        when(queueService.createQueue(eq(groupId), any(QueueRequest.class), "test@mail.ru")).thenReturn(new Queue());

        mockMvc.perform(post("/api/v1/groups/{groupId}/queues", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
