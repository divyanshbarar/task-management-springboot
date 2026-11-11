package com.divyansh.taskManagement.service;

import com.divyansh.taskManagement.dto.PaginatedResponse;
import com.divyansh.taskManagement.dto.TaskRequestDto;
import com.divyansh.taskManagement.dto.TaskResponseDto;
import com.divyansh.taskManagement.entity.TaskPriority;

import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto requestDto);

    TaskResponseDto getTaskById(Long id);

    TaskResponseDto updateTask(Long id, TaskRequestDto requestDto);

    void deleteTask(Long id);

    TaskResponseDto markTaskCompletion(Long id, boolean completed);

    List<TaskResponseDto> getAllTasks();

    PaginatedResponse<TaskResponseDto> getTasksPaginated(
            String keyword,
            Boolean completed,
            TaskPriority priority,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
