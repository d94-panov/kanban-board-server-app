package Backend.Board.service;

import Backend.Board.model.BoardRoleType;
import Backend.Board.model.User;
import Backend.Board.repository.UserBoardRoleRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class BoardSecurityService {
    @Autowired
    private UserBoardRoleRepository userBoardRoleRepository;

    public boolean hasBoardAccess(Authentication authentication, Long boardId) {
        User user = (User) authentication.getPrincipal();
        return userBoardRoleRepository.existsByUserAndBoardIdAndRoleIn(
                user,
                boardId,
                List.of(BoardRoleType.ADMIN, BoardRoleType.WRITER, BoardRoleType.READER)
        );
    }
}
