package com.divyansh.taskManagement.controller;

import com.divyansh.taskManagement.dto.PaginatedResponse;
import com.divyansh.taskManagement.dto.TaskRequestDto;
import com.divyansh.taskManagement.dto.TaskResponseDto;
import com.divyansh.taskManagement.entity.TaskPriority;
import com.divyansh.taskManagement.exception.GlobalExceptionHandler;
import com.divyansh.taskManagement.exception.ResourceNotFoundException;
import com.divyansh.taskManagement.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;
    private TaskResponseDto sampleResponseDto;
    private TaskRequestDto sampleRequestDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponseDto = TaskResponseDto.builder()
                .id(1L)
                .title("Complete Assignment")
                .description("Finish Spring Boot task application")
                .completed(false)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2026, 12, 31))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequestDto = TaskRequestDto.builder()
                .title("Complete Assignment")
                .description("Finish Spring Boot task application")
                .completed(false)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2026, 12, 31))
                .build();
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task and return 201 Created")
    void shouldCreateTask() throws Exception {
        when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(sampleResponseDto);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Complete Assignment")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 Bad Request when title is blank")
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        TaskRequestDto invalid = TaskRequestDto.builder()
                .title("") // Blank violates @NotBlank
                .description("Description")
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.title", is("Title cannot be empty")));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return 200 OK")
    void shouldGetTaskById() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleResponseDto);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Complete Assignment")));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return 404 when not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Task not found with id: 99")));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should update task and return 200 OK")
    void shouldUpdateTask() throws Exception {
        when(taskService.updateTask(eq(1L), any(TaskRequestDto.class))).thenReturn(sampleResponseDto);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Should return 204 No Content")
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/mark - Should mark task as completed")
    void shouldMarkTask() throws Exception {
        TaskResponseDto completedTask = TaskResponseDto.builder()
                .id(1L)
                .title("Complete Assignment")
                .completed(true)
                .build();

        when(taskService.markTaskCompletion(1L, true)).thenReturn(completedTask);

        mockMvc.perform(patch("/api/tasks/1/mark")
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return paginated and filtered tasks")
    void shouldGetTasksPaginated() throws Exception {
        PaginatedResponse<TaskResponseDto> paginatedResponse = PaginatedResponse.<TaskResponseDto>builder()
                .content(List.of(sampleResponseDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(taskService.getTasksPaginated(
                eq("Assignment"), eq(false), eq(TaskPriority.HIGH), eq(0), eq(10), eq("id"), eq("desc")
        )).thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/tasks")
                        .param("keyword", "Assignment")
                        .param("completed", "false")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Complete Assignment")));
    }
}
