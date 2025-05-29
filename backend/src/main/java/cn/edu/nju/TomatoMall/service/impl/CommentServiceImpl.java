package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.models.dto.comment.CommentDTO;
import cn.edu.nju.TomatoMall.models.po.Comment;
import cn.edu.nju.TomatoMall.repository.CommentRepository;
import cn.edu.nju.TomatoMall.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment = commentRepository.save(comment);
        return convertToDTO(comment);
    }


    @Override
    @Transactional
    public CommentDTO updateComment(int commentId, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        
        // 只更新允许修改的字段
        comment.setContent(commentDTO.getContent());
        comment.setRating(commentDTO.getRating());
        
        comment = commentRepository.save(comment);
        return convertToDTO(comment);
    }

    @Override
    public CommentDTO getCommentById(int commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        return convertToDTO(comment);
    }

    @Override
    public List<CommentDTO> getItemComments(int itemId) {
        return commentRepository.findByItemIdOrderByLikesCountDesc(itemId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getShopComments(int shopId) {
        return commentRepository.findByShopIdOrderByLikesCountDesc(shopId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getUserComments(int userId) {
        return commentRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> getItemCommentsPaged(int itemId, Pageable pageable) {
        return commentRepository.findByItemIdAndIsDeletedFalseOrderByCreatedAtDesc(itemId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<CommentDTO> getShopCommentsPaged(int shopId, Pageable pageable) {
        return commentRepository.findByShopIdAndIsDeletedFalseOrderByCreatedAtDesc(shopId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<CommentDTO> getUserCommentsPaged(int userId, Pageable pageable) {
        return commentRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CommentDTO likeComment(int commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        comment.setLikesCount(comment.getLikesCount() + 1);
        comment = commentRepository.save(comment);
        return convertToDTO(comment);
    }

    @Override
    @Transactional
    public CommentDTO replyToComment(int parentId, CommentDTO replyDTO) {
        // 验证父评论是否存在
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("父评论不存在"));
        
        Comment reply = new Comment();
        BeanUtils.copyProperties(replyDTO, reply);
        reply.setParentId(parentId);
        reply = commentRepository.save(reply);
        return convertToDTO(reply);
    }

    @Override
    @Transactional
    public void deleteComment(int commentId, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        if (comment.getUserId() != userId) {
            throw new RuntimeException("无权删除该评论");
        }
        commentRepository.softDelete(commentId);
    }

    @Override
    public Double getItemAverageRating(int itemId) {
        return commentRepository.getAverageRatingForItem(itemId);
    }

    @Override
    public Double getShopAverageRating(int shopId) {
        return commentRepository.getAverageRatingForShop(shopId);
    }

    @Override
    public long getItemCommentCount(int itemId) {
        return commentRepository.countByItemIdAndIsDeletedFalse(itemId);
    }

    @Override
    public long getShopCommentCount(int shopId) {
        return commentRepository.countByShopIdAndIsDeletedFalse(shopId);
    }

    @Override
    public long getUserCommentCount(int userId) {
        return commentRepository.countByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<CommentDTO> getReplies(int commentId) {
        return commentRepository.findByParentIdOrderByCreatedAtDesc(commentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        return dto;
    }
} 