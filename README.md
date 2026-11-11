# Task Management Spring Boot Application

A robust, beginner-to-intermediate Task Management REST API built with **Spring Boot 3**, **PostgreSQL**, **Spring Data JPA**, **ModelMapper**, and **Lombok**.

---

## 🏗️ Architecture

```
Client (Postman / Browser / Frontend)
                 │  (JSON Request / Response)
                 ▼
       [TaskController]  (REST Endpoints, Validation)
                 │  (TaskRequestDto / TaskResponseDto)
                 ▼
         [TaskService]   ◄── [ModelMapper] (Entity ◄─► DTO mapping)
                 │  (Task Entity)
                 ▼
        [TaskRepository] (Spring Data JPA with Pageable, JPQL Query)
                 │  (SQL / Hibernate)
                 ▼
      [PostgreSQL Database] (taskmanagement_db.tasks)
```

---

## 🌟 Features Implemented

1. **Auto-Generated ID**: Uses `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
2. **Add Task**: Validates input (`@NotBlank`, `@Size`) and returns `201 Created`.
3. **Update Task**: Updates `title`, `description`, `completed`, `priority`, `dueDate`.
4. **Delete Task**: Deletes by ID; throws `ResourceNotFoundException` (404) if not found.
5. **Mark Task**: Quickly mark/unmark a task as completed (`PATCH /api/tasks/{id}/mark?completed=true`).
6. **Search**: Keyword search across `title` and `description` (case-insensitive).
7. **Filter**: Filter by completion status (`completed=true|false`) and/or `priority` (`LOW`, `MEDIUM`, `HIGH`).
8. **Sorting**: Sort by any field (`id`, `title`, `createdAt`, `dueDate`, `completed`) in `asc` or `desc` order.
9. **Pagination**: Paginated results with `page`, `size`, `totalElements`, `totalPages`, `isFirst`, `isLast`.
10. **Global Exception Handling**: Centralized `@RestControllerAdvice` returning formatted JSON error responses for 404, 400 validation, and 500 errors.

---

## 🗄️ PostgreSQL Database Configuration

Located in `src/main/resources/application.properties`:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanagement_db
spring.datasource.username=postgres
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

---

## 🚀 API Endpoints & Examples

### 1. Add Task (Create)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/tasks`
- **Request Body**:
```json
{
  "title": "Prepare Presentation",
  "description": "Create slides for project demo",
  "completed": false,
  "priority": "HIGH",
  "dueDate": "2026-11-20"
}
```
- **Response**: `201 Created`
```json
{
  "id": 6,
  "title": "Prepare Presentation",
  "description": "Create slides for project demo",
  "completed": false,
  "priority": "HIGH",
  "dueDate": "2026-11-20",
  "createdAt": "2026-11-04T03:40:23",
  "updatedAt": "2026-11-04T03:40:23"
}
```

---

### 2. Update Task
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/tasks/3`
- **Request Body**:
```json
{
  "title": "Implement DTOs and ModelMapper",
  "description": "ModelMapper configured with clean DTO mapping",
  "completed": true,
  "priority": "MEDIUM",
  "dueDate": "2026-11-08"
}
```
- **Response**: `200 OK`

---

### 3. Mark Task as Completed / Incomplete
- **Method**: `PATCH`
- **URL**: `http://localhost:8080/api/tasks/4/mark?completed=true`
- **Response**: `200 OK` (returns updated task with `"completed": true`)

---

### 4. Delete Task
- **Method**: `DELETE`
- **URL**: `http://localhost:8080/api/tasks/1`
- **Response**: `204 No Content`

---

### 5. Search, Filter, Sort & Paginate Tasks
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/tasks`
- **Query Parameters**:
  - `page`: Page index (default `0`)
  - `size`: Items per page (default `10`)
  - `sortBy`: Field name to sort by (default `id`, or `title`, `dueDate`, `createdAt`, `completed`)
  - `sortDir`: Sort direction (`asc` or `desc`, default `desc`)
  - `keyword`: (Optional) Search text in title & description
  - `completed`: (Optional) Filter by `true` or `false`
  - `priority`: (Optional) Filter by `LOW`, `MEDIUM`, `HIGH`

#### Example Queries:
- **Search keyword "Spring"**:
  `GET http://localhost:8080/api/tasks?keyword=Spring`
- **Filter incomplete tasks only**:
  `GET http://localhost:8080/api/tasks?completed=false`
- **Filter HIGH priority completed tasks, sorted by dueDate**:
  `GET http://localhost:8080/api/tasks?completed=true&priority=HIGH&sortBy=dueDate&sortDir=asc`
- **Pagination (Page 0, 2 tasks per page)**:
  `GET http://localhost:8080/api/tasks?page=0&size=2`

- **Response Body**:
```json
{
  "content": [
    {
      "id": 5,
      "title": "Deploy Application",
      "description": "Containerize or deploy to cloud server",
      "completed": false,
      "priority": "LOW",
      "dueDate": "2026-11-14",
      "createdAt": "2026-11-04T03:40:23",
      "updatedAt": "2026-11-04T03:40:23"
    },
    {
      "id": 4,
      "title": "Add Pagination and Sorting",
      "description": "Test PageRequest with sort direction and size",
      "completed": false,
      "priority": "MEDIUM",
      "dueDate": "2026-11-09",
      "createdAt": "2026-11-04T03:40:23",
      "updatedAt": "2026-11-04T03:40:23"
    }
  ],
  "pageNumber": 0,
  "pageSize": 2,
  "totalElements": 5,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

---

### 6. Validation Error Example
If `title` is blank:
- **Request**: `POST /api/tasks` with `{"title": ""}`
- **Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-11-04T03:40:23",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "validationErrors": {
    "title": "Title cannot be empty"
  }
}
```

---

## 💻 Running the Application

### Via Command Line:
```powershell
.\mvnw.cmd spring-boot:run
```

### Via IntelliJ IDEA:
1. Ensure Java Compiler bytecode version is **17** in **Settings > Build, Execution, Deployment > Compiler > Java Compiler**.
2. Run `TaskManagementApplication.java` (or press `Shift + F10`).
