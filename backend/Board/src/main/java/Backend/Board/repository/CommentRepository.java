package Backend.Board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Backend.Board.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}