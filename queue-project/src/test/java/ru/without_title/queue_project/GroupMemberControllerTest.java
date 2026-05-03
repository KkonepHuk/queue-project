package ru.without_title.queue_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.without_title.queue_project.database.entities.*;
import ru.without_title.queue_project.database.entities.enums.*;
import ru.without_title.queue_project.services.*;
import ru.without_title.queue_project.controllers.*;
import ru.without_title.queue_project.dto.request.*;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(GroupMemberController.class)
class GroupMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupMemberService groupMemberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateMemberRole() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        GroupMemberUpdateRequest request = new GroupMemberUpdateRequest(GroupRole.OWNER);

        // Имитируем возврат сущности (упрощенно)
        GroupMember updatedMember = new GroupMember();
        // ... настройка полей ...

        when(groupMemberService.updateRole(eq(groupId), eq(memberId), any(GroupRole.class)))
                .thenReturn(updatedMember);

        mockMvc.perform(patch("/api/v1/groups/{groupId}/members/{memberId}", groupId, memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
