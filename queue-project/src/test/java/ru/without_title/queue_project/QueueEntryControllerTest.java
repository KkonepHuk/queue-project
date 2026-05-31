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
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueEntryController.class)
class QueueEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueEntryService entryService;

    @Test
    void shouldJoinQueue() throws Exception {
        UUID queueId = UUID.randomUUID();

        // void методы в Mockito по умолчанию ничего не делают,
        // но мы можем явно это указать (необязательно)
        doNothing().when(entryService).joinQueue(eq(queueId), "test@mail.ru");

        mockMvc.perform(post("/api/v1/queues/{queueId}/join", queueId))
                .andExpect(status().isNoContent());

        // Проверяем, что метод сервиса реально вызвался
        verify(entryService, times(1)).joinQueue(queueId, "test@mail.ru");
    }
}
