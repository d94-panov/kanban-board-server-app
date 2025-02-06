package Backend.Board.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Backend.Board.dto.CommentDTO;
import Backend.Board.dto.TaskDTO;
import Backend.Board.model.Task;
import Backend.Board.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

   @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
            .map(task -> {
                List<CommentDTO> commentDTOs = task.getComments().stream()
                    .map(comment -> new CommentDTO(comment.getId(), comment.getContent()))
                    .collect(Collectors.toList());

                TaskDTO taskDTO = new TaskDTO(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    commentDTOs
                );
                return ResponseEntity.ok(taskDTO);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new task.
     * 
     * @param task The task data to create.
     * @return The created task.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Title is required
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Updates an existing task.
     * 
     * @param id          The ID of the task to update.
     * @param updatedTask The updated task data.
     * @return The updated task, or a 404 error if the task is not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        if (updatedTask.getTitle() == null || updatedTask.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Title is required
        }
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    // task.setColumnId(updatedTask.getColumnId());
                    Task savedTask = taskRepository.save(task);
                    return ResponseEntity.ok(savedTask);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Deletes a task by ID.
     * 
     * @param id The ID of the task to delete.
     * @return A 204 No Content response if successful, or a 404 error if the task
     *         is not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    taskRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}