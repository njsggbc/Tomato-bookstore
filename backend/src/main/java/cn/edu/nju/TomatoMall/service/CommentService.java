package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.comment.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    CommentDTO createComment(CommentDTO commentDTO);
    CommentDTO updateComment(int commentId, CommentDTO commentDTO);
    void deleteComment(int commentId, int userId);
    CommentDTO getCommentById(int commentId);
    List<CommentDTO> getItemComments(int itemId);
    List<CommentDTO> getShopComments(int shopId);
    List<CommentDTO> getUserComments(int userId);
    Page<CommentDTO> getItemCommentsPaged(int itemId, Pageable pageable);
    Page<CommentDTO> getShopCommentsPaged(int shopId, Pageable pageable);
    Page<CommentDTO> getUserCommentsPaged(int userId, Pageable pageable);
    CommentDTO likeComment(int commentId);
    CommentDTO replyToComment(int parentId, CommentDTO replyDTO);
    Double getItemAverageRating(int itemId);
    Double getShopAverageRating(int shopId);
    long getItemCommentCount(int itemId);
    long getShopCommentCount(int shopId);
    long getUserCommentCount(int userId);
    List<CommentDTO> getReplies(int commentId);
} 