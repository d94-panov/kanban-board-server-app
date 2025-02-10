package Backend.Board.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import Backend.Board.dto.BoardDTO;
import Backend.Board.dto.ColumnDTO;
import Backend.Board.dto.TaskPreviewDTO;
import Backend.Board.mappers.BoardMapper;
import Backend.Board.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import Backend.Board.model.Column;
import Backend.Board.model.Task;
import Backend.Board.repository.BoardRepository;
import Backend.Board.repository.TaskRepository;
import jakarta.transaction.Transactional;

@RestController
@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private TaskRepository taskRepository;

    private static final List<String> COLUMN_NAMES = Arrays.asList("Backlog", "To Do", "In Progress", "Review", "Done");

    @GetMapping
    public List<BoardDTO> getAllBoards(@RequestParam(required = false) Long id) {
        List<Board> boards = id != null ? boardRepository.findById(id).map(List::of)
                .orElseThrow(() -> new RuntimeException("Board not found")) : boardRepository.findAll();

        return boards.stream()
                .map(BoardMapper::toDTO)
                .collect(Collectors.toList());
    }

    @MessageMapping("/board/{boardId}/update")
    @SendTo("/topic/board/{boardId}")
    @Transactional
    public BoardDTO updateBoard(
            @DestinationVariable Long boardId,
            Message<BoardDTO> message
    ) {
        System.out.println("updateBoard for board: " + boardId);

        BoardDTO updatedBoardDTO = message.getPayload();
        System.out.println("Received board update: " + updatedBoardDTO);

        Board existingBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));

        existingBoard.setName(updatedBoardDTO.getName());

        updateColumns(existingBoard, updatedBoardDTO.getColumns());

        Board savedBoard = boardRepository.save(existingBoard);

        return BoardMapper.toDTO(savedBoard);
    }

    private void updateColumns(Board existingBoard, List<ColumnDTO> updatedColumns) {
        Map<Long, Column> existingColumnsMap = existingBoard.getColumns().stream()
                .collect(Collectors.toMap(Column::getId, c -> c));

        List<Column> newColumns = new ArrayList<>();

        for (ColumnDTO columnDTO : updatedColumns) {
            Column column = existingColumnsMap.getOrDefault(columnDTO.getId(), new Column());
            column.setName(columnDTO.getName());
            column.setBoard(existingBoard);

            updateTasks(column, columnDTO.getTasks());

            newColumns.add(column);
            existingColumnsMap.remove(column.getId());
        }

        existingBoard.getColumns().removeAll(existingColumnsMap.values());

        existingBoard.getColumns().clear();
        existingBoard.getColumns().addAll(newColumns);
    }

    private void updateTasks(Column column, List<TaskPreviewDTO> updatedTasks) {
        Map<Long, Task> existingTasksMap = column.getTasks().stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        List<Task> newTasks = new ArrayList<>();

        for (TaskPreviewDTO taskDTO : updatedTasks) {
            Task task = existingTasksMap.getOrDefault(taskDTO.getId(), new Task());
            Optional<Task> taksInRepo = taskRepository.findById(taskDTO.getId());
            task.setColumn(column);
            task.setTitle(taksInRepo.get().getTitle());
            task.setDescription(taksInRepo.get().getDescription());
            task.setComments(taksInRepo.get().getComments());

            newTasks.add(task);
            existingTasksMap.remove(task.getId());
        }

        column.getTasks().removeAll(existingTasksMap.values());

        column.getTasks().clear();
        column.getTasks().addAll(newTasks);
    }

    @PostMapping
    public BoardDTO createBoard(@RequestBody BoardDTO board) {
        return BoardMapper.toDTO(boardRepository.save(BoardMapper.toEntity(board)));
    }

    @DeleteMapping
    public void deleteBoard(@RequestParam Long id) {
        boardRepository.deleteById(id);
    }

    @PostMapping("/random")
    public ResponseEntity<BoardDTO> createRandomBoard() {
        try {
            Random random = new Random();
            Board board = new Board();
            board.setName("Board-" + UUID.randomUUID().toString().substring(0, 8)); // Shorter ID

            List<Column> columns = new ArrayList<>();
            int columnCount = 2 + random.nextInt(4);

            List<String> shuffledColumnNames = new ArrayList<>(COLUMN_NAMES);
            Collections.shuffle(shuffledColumnNames);

            for (int i = 0; i < columnCount; i++) {
                Column column = new Column();
                String columnName = i < shuffledColumnNames.size()
                        ? shuffledColumnNames.get(i)
                        : "Column " + (i + 1);
                column.setName(columnName);
                column.setBoard(board);

                List<Task> tasks = new ArrayList<>();
                int taskCount = 1 + random.nextInt(6);

                for (int j = 0; j < taskCount; j++) {
                    Task task = new Task();
                    task.setTitle("Task " + (j + 1));
                    task.setDescription("Sample description for " + columnName);
                    task.setColumn(column);
                    tasks.add(task);
                }
                column.setTasks(tasks);
                columns.add(column);
            }

            board.setColumns(columns);
            Board savedBoard = boardRepository.save(board);
            return ResponseEntity.ok(BoardMapper.toDTO(savedBoard));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}