package Backend.Board.repository;

import Backend.Board.model.Board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.columns c LEFT JOIN FETCH c.tasks WHERE b.id = :id")
    Optional<Board> findByIdWithTasks(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.columns c LEFT JOIN FETCH c.tasks")
    List<Board> findAllWithTasks();
}