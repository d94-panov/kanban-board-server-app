package Backend.Board.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import Backend.Board.dto.BoardDTO;
import Backend.Board.dto.ColumnDTO;
import Backend.Board.dto.TaskPreviewDTO;
import Backend.Board.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import Backend.Board.model.Column;
import Backend.Board.model.Task;
import Backend.Board.repository.BoardRepository;

@RestController
@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;

    private static final List<String> COLUMN_NAMES = Arrays.asList("Backlog", "To Do", "In Progress", "Review", "Done");

    @GetMapping
    public List<BoardDTO> getAllBoards(@RequestParam(required = false) Long id) {
        List<Board> boards = id != null ? boardRepository.findById(id).map(List::of)
                .orElseThrow(() -> new RuntimeException("Board not found")) : boardRepository.findAll();

        return boards.stream()
                .map(this::convertToBoardDTO)
                .collect(Collectors.toList());
    }

    private BoardDTO convertToBoardDTO(Board board) {
        List<ColumnDTO> columnDTOs = board.getColumns().stream()
                .map(this::convertToColumnDTO)
                .collect(Collectors.toList());

        return new BoardDTO(
                board.getId(),
                board.getName(),
                columnDTOs);
    }

    private ColumnDTO convertToColumnDTO(Column column) {
        List<TaskPreviewDTO> taskDTOs = column.getTasks().stream()
                .map(task -> new TaskPreviewDTO(task.getId(), task.getTitle()))
                .collect(Collectors.toList());

        return new ColumnDTO(
                column.getId(),
                column.getName(),
                taskDTOs);
    }

    @MessageMapping("/update")
    @SendTo("/getUpdates")
    public BoardDTO updateBoard(@RequestParam Long id, @RequestBody Board board) throws Exception {
        this.boardRepository.getReferenceById(id).setColumns(board.getColumns());
        return this.convertToBoardDTO(this.boardRepository.getReferenceById(id));
    }

    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardRepository.save(board);
    }

    @DeleteMapping
    public void deleteBoard(@RequestParam Long id) {
        boardRepository.deleteById(id);
    }

    @PostMapping("/random")
    public Board createRandomBoard() {
        Random random = new Random();
        Board board = new Board();
        board.setName("Board " + UUID.randomUUID());
        List<Column> columns = new ArrayList<>();
        int columnCount = 2 + random.nextInt(4);

        for (int i = 0; i < columnCount; i++) {
            Column column = new Column();
            column.setName(COLUMN_NAMES.get(random.nextInt(COLUMN_NAMES.size())));
            column.setBoard(board);
            List<Task> tasks = new ArrayList<>();
            int taskCount = random.nextInt(7);

            for (int j = 0; j < taskCount; j++) {
                Task task = new Task();
                task.setTitle("Task " + UUID.randomUUID());
                task.setDescription("Description for task " + j);
                task.setColumn(column);
                tasks.add(task);
            }
            column.setTasks(tasks);
            columns.add(column);
        }
        board.setColumns(columns);
        return boardRepository.save(board);
    }
}