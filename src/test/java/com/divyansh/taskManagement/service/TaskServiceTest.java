package com.divyansh.taskManagement.service;

import com.divyansh.taskManagement.dto.PaginatedResponse;
import com.divyansh.taskManagement.dto.TaskRequestDto;
import com.divyansh.taskManagement.dto.TaskResponseDto;
import com.divyansh.taskManagement.entity.Task;
import com.divyansh.taskManagement.entity.TaskPriority;
import com.divyansh.taskManagement.exception.ResourceNotFoundException;
import com.divyansh.taskManagement.repository.TaskRepository;
import com.divyansh.taskManagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;
    private TaskRequestDto sampleRequestDto;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Complete Assignment")
                .description("Finish Spring Boot task application")
                .completed(false)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequestDto = TaskRequestDto.builder()
                .title("Complete Assignment")
                .description("Finish Spring Boot task application")
                .completed(false)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    @DisplayName("Should create task with auto-generated ID and default completed status")
    void shouldCreateTaskSuccessfully() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponseDto result = taskService.createTask(sampleRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Complete Assignment");
        assertThat(result.getCompleted()).isFalse();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should return task by ID")
    void shouldReturnTaskByIdWhenExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        TaskResponseDto result = taskService.getTaskById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task does not exist")
    void shouldThrowExceptionWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 99");
    }

    @Test
    @DisplayName("Should update task title, description, and completed")
    void shouldUpdateTaskSuccessfully() {
        TaskRequestDto updateRequest = TaskRequestDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .completed(true)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto updated = taskService.updateTask(1L, updateRequest);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getCompleted()).isTrue();
        assertThat(updated.getPriority()).isEqualTo(TaskPriority.LOW);
    }

    @Test
    @DisplayName("Should delete task when it exists")
    void shouldDeleteTaskSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should mark task as completed")
    void shouldMarkTaskAsCompleted() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = taskService.markTaskCompletion(1L, true);

        assertThat(result.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should return paginated and filtered tasks")
    void shouldReturnPaginatedTasks() {
        Page<Task> page = new PageImpl<>(List.of(sampleTask));
        when(taskRepository.searchAndFilter(eq("Assignment"), eq(false), eq(TaskPriority.HIGH), any(Pageable.class)))
                .thenReturn(page);

        PaginatedResponse<TaskResponseDto> response = taskService.getTasksPaginated(
                "Assignment", false, TaskPriority.HIGH, 0, 10, "id", "desc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Complete Assignment");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }
}
