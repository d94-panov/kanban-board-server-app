package Backend.Board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import Backend.Board.model.Comment;
import Backend.Board.repository.CommentRepository;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping
    public List<Comment> getAllComments(@RequestParam(required = false) Long id) {
        if (id != null) {
            return commentRepository.findById(id).map(List::of)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
        }
        return commentRepository.findAll();
    }

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentRepository.save(comment);
    }

    @DeleteMapping
    public void deleteComment(@RequestParam Long id) {
        commentRepository.deleteById(id);
    }
}