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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "test@mail.ru", "password123", "Иван", "Иванов", "88005553535");

        // Предположим, сервис возвращает созданного юзера с ID
        User savedUser = new User();
        savedUser.setUserId(UUID.randomUUID());
        savedUser.setEmail(request.getEmail());

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.userId").exists());
    }
}
