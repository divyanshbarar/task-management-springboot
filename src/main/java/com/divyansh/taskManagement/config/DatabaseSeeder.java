package com.divyansh.taskManagement.config;

import com.divyansh.taskManagement.entity.Task;
import com.divyansh.taskManagement.entity.TaskPriority;
import com.divyansh.taskManagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (taskRepository.count() == 0) {
            log.info("Seeding initial task data for pagination and testing...");

            Task task1 = Task.builder()
                    .title("Learn Spring Boot 3")
                    .description("Study Controller, Service, and Repository patterns")
                    .completed(true)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.now().minusDays(2))
                    .build();

            Task task2 = Task.builder()
                    .title("Setup PostgreSQL Database")
                    .description("Connect to taskmanagement_db on port 5432")
                    .completed(true)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.now().minusDays(1))
                    .build();

            Task task3 = Task.builder()
                    .title("Implement DTOs and ModelMapper")
                    .description("Map Request and Response DTOs without boilerplate")
                    .completed(false)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(LocalDate.now().plusDays(3))
                    .build();

            Task task4 = Task.builder()
                    .title("Add Pagination and Sorting")
                    .description("Test PageRequest with sort direction and size")
                    .completed(false)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(LocalDate.now().plusDays(5))
                    .build();

            Task task5 = Task.builder()
                    .title("Deploy Application")
                    .description("Containerize or deploy to cloud server")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.now().plusDays(10))
                    .build();

            taskRepository.saveAll(List.of(task1, task2, task3, task4, task5));
            log.info("Successfully seeded 5 initial tasks.");
        }
    }
}
