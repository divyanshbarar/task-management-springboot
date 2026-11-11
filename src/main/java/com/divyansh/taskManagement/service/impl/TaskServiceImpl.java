package com.divyansh.taskManagement.service.impl;

import com.divyansh.taskManagement.dto.PaginatedResponse;
import com.divyansh.taskManagement.dto.TaskRequestDto;
import com.divyansh.taskManagement.dto.TaskResponseDto;
import com.divyansh.taskManagement.entity.Task;
import com.divyansh.taskManagement.entity.TaskPriority;
import com.divyansh.taskManagement.exception.ResourceNotFoundException;
import com.divyansh.taskManagement.repository.TaskRepository;
import com.divyansh.taskManagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Override
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        Task task = modelMapper.map(requestDto, Task.class);
        if (task.getCompleted() == null) {
            task.setCompleted(false);
        }
        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return modelMapper.map(task, TaskResponseDto.class);
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Update title, description, completed, and other optional fields
        existingTask.setTitle(requestDto.getTitle());
        existingTask.setDescription(requestDto.getDescription());
        if (requestDto.getCompleted() != null) {
            existingTask.setCompleted(requestDto.getCompleted());
        }
        if (requestDto.getPriority() != null) {
            existingTask.setPriority(requestDto.getPriority());
        }
        existingTask.setDueDate(requestDto.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);
        return modelMapper.map(updatedTask, TaskResponseDto.class);
    }

    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponseDto markTaskCompletion(Long id, boolean completed) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        task.setCompleted(completed);
        Task updatedTask = taskRepository.save(task);
        return modelMapper.map(updatedTask, TaskResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> modelMapper.map(task, TaskResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TaskResponseDto> getTasksPaginated(
            String keyword,
            Boolean completed,
            TaskPriority priority,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Task> taskPage = taskRepository.searchAndFilter(trimmedKeyword, completed, priority, pageable);

        List<TaskResponseDto> dtoList = taskPage.getContent()
                .stream()
                .map(task -> modelMapper.map(task, TaskResponseDto.class))
                .collect(Collectors.toList());

        return PaginatedResponse.<TaskResponseDto>builder()
                .content(dtoList)
                .pageNumber(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .isLast(taskPage.isLast())
                .isFirst(taskPage.isFirst())
                .build();
    }
}
