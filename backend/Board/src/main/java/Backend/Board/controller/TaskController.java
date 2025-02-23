package Backend.Board.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import Backend.Board.dto.BoardDTO;
import Backend.Board.dto.CommentDTO;
import Backend.Board.dto.TaskDTO;
import Backend.Board.mappers.BoardMapper;
import Backend.Board.model.Task;
import Backend.Board.repository.BoardRepository;
import Backend.Board.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    List<CommentDTO> commentDTOs = task.getComments().stream()
                            .map(comment -> new CommentDTO(comment.getId(), comment.getContent()))
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(new TaskDTO(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            commentDTOs
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Task savedTask = taskRepository.save(task);
        sendBoardUpdate(savedTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        if (updatedTask.getTitle() == null || updatedTask.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    Task savedTask = taskRepository.save(task);
                    sendBoardUpdate(savedTask.getId());
                    return ResponseEntity.ok(savedTask);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Long id) {
        return taskRepository.findByIdWithColumnAndBoard(id)
                .map(task -> {
                    Long boardId = task.getColumn().getBoard().getId();
                    taskRepository.delete(task);
                    taskRepository.flush();  // Critical for immediate sync

                    sendBoardUpdateDirect(boardId);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void sendBoardUpdateDirect(Long boardId) {
        boardRepository.findById(boardId)
                .ifPresent(board -> {
                    BoardDTO dto = BoardMapper.toDTO(board);
                    messagingTemplate.convertAndSend(
                            "/topic/board/" + boardId,
                            dto
                    );
                });
    }

    private void sendBoardUpdate(Long taskId) {
        taskRepository.findByIdWithColumnAndBoard(taskId)
                .ifPresent(task -> {
                    Long boardId = task.getColumn().getBoard().getId();
                    boardRepository.findById(boardId)
                            .ifPresent(board -> {
                                BoardDTO dto = BoardMapper.toDTO(board);
                                messagingTemplate.convertAndSend(
                                        "/topic/board/" + boardId,
                                        dto
                                );
                            });
                });
    }
}
