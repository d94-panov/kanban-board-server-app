package Backend.Board.repository;

import Backend.Board.model.UserBoardRole;
import Backend.Board.model.BoardRoleType;
import Backend.Board.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBoardRoleRepository extends JpaRepository<UserBoardRole, Long> {
    boolean existsByUserAndBoardIdAndRoleIn(User user, Long boardId, List<BoardRoleType> roles);
}