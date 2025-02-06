package Backend.Board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Backend.Board.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {}