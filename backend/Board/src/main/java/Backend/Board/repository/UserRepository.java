package Backend.Board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Backend.Board.model.User;

public interface UserRepository extends JpaRepository<User, Long> {}