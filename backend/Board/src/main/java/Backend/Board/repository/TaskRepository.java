package Backend.Board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Backend.Board.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {}