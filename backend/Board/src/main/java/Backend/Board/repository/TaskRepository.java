package Backend.Board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Backend.Board.model.Task;

// Add to your TaskRepository interface
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t JOIN FETCH t.column c JOIN FETCH c.board WHERE t.id = :id")
    Optional<Task> findByIdWithColumnAndBoard(@Param("id") Long id);
}