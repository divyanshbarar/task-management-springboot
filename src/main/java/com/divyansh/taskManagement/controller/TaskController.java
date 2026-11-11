package com.divyansh.taskManagement.controller;

import com.divyansh.taskManagement.dto.PaginatedResponse;
import com.divyansh.taskManagement.dto.TaskRequestDto;
import com.divyansh.taskManagement.dto.TaskResponseDto;
import com.divyansh.taskManagement.entity.TaskPriority;
import com.divyansh.taskManagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 1. Add Task (Create)
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto createdTask = taskService.createTask(requestDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // 2. Get Task by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // 3. Update Task (title, description, completed, priority, dueDate)
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto updatedTask = taskService.updateTask(id, requestDto);
        return ResponseEntity.ok(updatedTask);
    }

    // 4. Delete Task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Mark Task (completed = true / false)
    @PatchMapping("/{id}/mark")
    public ResponseEntity<TaskResponseDto> markTask(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean completed) {
        TaskResponseDto task = taskService.markTaskCompletion(id, completed);
        return ResponseEntity.ok(task);
    }

    // 6. Get Tasks with Search, Filter, Sorting, and Pagination
    @GetMapping
    public ResponseEntity<PaginatedResponse<TaskResponseDto>> getTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PaginatedResponse<TaskResponseDto> response = taskService.getTasksPaginated(
                keyword, completed, priority, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(response);
    }

    // 7. Get All Tasks (unpaginated convenience endpoint)
    @GetMapping("/all")
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
}
