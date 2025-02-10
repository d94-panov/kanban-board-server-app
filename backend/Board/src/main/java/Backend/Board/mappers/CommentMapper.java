package Backend.Board.mappers;

import Backend.Board.dto.CommentDTO;
import Backend.Board.model.Comment;

public class CommentMapper {
    public static CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        return commentDTO;
    }

    public static Comment toEntity(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDTO.getId());
        comment.setContent(commentDTO.getContent());
        return comment;
    }
}
