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

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateGroupWithFullDetails() throws Exception {
        UUID adminId = UUID.randomUUID();
        // Используем твой структуру DTO
        GroupCreateRequest request = new GroupCreateRequest("БПИ-221", "Группа для зачетов", adminId);

        Group savedGroup = new Group();
        savedGroup.setGroupId(UUID.randomUUID());
        savedGroup.setName("БПИ-221");

        when(groupService.createGroup(any(GroupCreateRequest.class))).thenReturn(savedGroup);

        mockMvc.perform(post("/api/v1/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("БПИ-221"));
    }
}
