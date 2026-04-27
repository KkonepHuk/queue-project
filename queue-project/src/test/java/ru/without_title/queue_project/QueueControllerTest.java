package ru.without_title.queue_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.dto.request.GroupCreateRequest;
import ru.without_title.queue_project.services.GroupService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
                30);

        when(queueService.createQueue(eq(groupId), any(QueueRequest.class))).thenReturn(new Queue());

        mockMvc.perform(post("/api/v1/groups/{groupId}/queues", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
