package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreRole;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.comment.CommentResponse;
import cn.edu.nju.TomatoMall.models.po.Comment;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.*;
import cn.edu.nju.TomatoMall.service.CommentService;
import cn.edu.nju.TomatoMall.service.PermissionService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private PermissionService permissionService;

    @Override
    public void comment(EntityType entityType, Integer entityId, String content, Integer rating) {
        User currentUser = securityUtil.getCurrentUser();

        // 验证评分
        if (rating != null && (rating < 1 || rating > 10)) {
            throw TomatoMallException.invalidParameter("评分必须在1-10之间");
        }

        // 验证评论权限
        validatePermission(entityType, entityId, currentUser);

        // 创建评论
        Comment comment = Comment.builder()
                .user(currentUser)
                .entityType(entityType)
                .entityId(entityId)
                .content(content)
                .rating(rating)
                .build();

        commentRepository.save(comment);

        // 更新实体评分
        if (rating != null) {
            updateEntityRating(entityType, entityId);
        }
    }

    @Override
    public void reply(int parentId, String content) {
        User currentUser = securityUtil.getCurrentUser();

        // 查找评论
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(TomatoMallException::commentNotFound);

        // 创建回复（回复不能有评分）
        Comment reply = Comment.builder()
                .user(currentUser)
                .entityType(parentComment.getEntityType())
                .entityId(parentComment.getEntityId())
                .content(content)
                .parent(parentComment)
                .build();

        commentRepository.save(reply);
    }

    @Override
    public void update(int commentId, String content, Integer rating) {
        User currentUser = securityUtil.getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(TomatoMallException::commentNotFound);

        // 只有评论作者可以修改
        if (!comment.getUser().equals(currentUser)) {
            throw TomatoMallException.permissionDenied();
        }

        // 回复不能有评分
        if (comment.getParent() != null && rating != null) {
            throw TomatoMallException.invalidParameter("回复不能有评分");
        }

        // 验证评分
        if (rating != null && (rating < 1 || rating > 10)) {
            throw TomatoMallException.invalidParameter("评分必须在1-10之间");
        }

        boolean ratingChanged = false;
        // 更新内容
        if (content != null) {
            comment.setContent(content);
        }
        // 更新评分
        if (rating != null && !rating.equals(comment.getRating())) {
            comment.setRating(rating);
            ratingChanged = true;
        }

        comment.setUpdateTime(LocalDateTime.now());
        commentRepository.save(comment);

        // 如果评分发生变化，更新实体评分
        if (ratingChanged && comment.getParent() == null) {
            updateEntityRating(comment.getEntityType(), comment.getEntityId());
        }
    }

    @Override
    public void delete(int commentId) {
        User currentUser = securityUtil.getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(TomatoMallException::commentNotFound);

        // 检查权限：评论作者或管理员可以删除
        if (!comment.getUser().equals(currentUser)
                && !currentUser.getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        // 记录是否需要更新评分
        boolean shouldUpdateRating = comment.getRating() != null && comment.getParent() == null;
        EntityType entityType = comment.getEntityType();
        int entityId = comment.getEntityId();

        // 删除所有回复
        commentRepository.deleteAllByParentId(commentId);
        // 删除评论
        commentRepository.delete(comment);

        // 删除后更新评分
        if (shouldUpdateRating) {
            updateEntityRating(entityType, entityId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(EntityType entityType, int entityId,
                                             int page, int size, String field, boolean order) {
        Sort sort = order ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> comments = commentRepository.findByEntityTypeAndEntityIdAndParentIsNull(
                entityType, entityId, pageable);

        return comments.map(comment -> {
            User currentUser = securityUtil.getCurrentUser();
            boolean liked = comment.isLikedBy(currentUser.getId());
            return new CommentResponse(comment, liked);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getReplies(int parentId, int page, int size, String field, boolean order) {
        if (!commentRepository.existsById(parentId)) {
            throw TomatoMallException.commentNotFound();
        }

        Sort sort = order ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> replies = commentRepository.findByParentId(parentId, pageable);

        return replies.map(comment -> {
            User currentUser = securityUtil.getCurrentUser();
            boolean liked = comment.isLikedBy(currentUser.getId());
            return new CommentResponse(comment, liked);
        });
    }

    @Override
    public void toggleLike(int commentId) {
        User currentUser = securityUtil.getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(TomatoMallException::commentNotFound);

        comment.toggleLike(currentUser.getId());
        commentRepository.save(comment);
    }

    private void validatePermission(EntityType entityType, int entityId, User currentUser) {
        switch (entityType) {
            case STORE:
                if (!permissionService.getStoreRole(entityId).equals(StoreRole.CUSTOMER)) {
                    throw TomatoMallException.permissionDenied();
                }
                break;
            case PRODUCT:
                if (!permissionService.getStoreRole(
                        productRepository.findStoreIdById(entityId).orElseThrow(TomatoMallException::productNotFound)
                ).equals(StoreRole.CUSTOMER)) {
                    throw TomatoMallException.permissionDenied();
                }
                break;
            default:
                throw TomatoMallException.invalidParameter();
        }

        if (commentRepository.findUserCommentOnEntity(currentUser.getId(), entityType, entityId).isPresent()) {
            throw TomatoMallException.permissionDenied("不能重复评论");
        }
    }

    private void updateEntityRating(EntityType entityType, int entityId) {
        Double avgRating = commentRepository.calculateAverageRating(entityType, entityId);

        if (avgRating != null) {
            // 保留一位小数，向上取整
            BigDecimal rating = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.CEILING);

            switch (entityType) {
                case STORE:
                    storeRepository.findById(entityId).ifPresent(store -> {
                        store.setRating(rating);
                        storeRepository.save(store);
                    });
                    break;
                case PRODUCT:
                    productRepository.findById(entityId).ifPresent(product -> {
                        product.setRating(rating);
                        productRepository.save(product);
                    });
                    break;
            }
        }
    }
}