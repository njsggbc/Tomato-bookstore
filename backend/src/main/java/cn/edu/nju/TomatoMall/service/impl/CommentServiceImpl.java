package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.models.dto.comment.*;
import cn.edu.nju.TomatoMall.models.po.Comment;
import cn.edu.nju.TomatoMall.models.po.Product;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.repository.CommentRepository;
import cn.edu.nju.TomatoMall.repository.ProductRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.CommentService;
import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    @Transactional
    public void createComment(CommentCreateRequest commentCreateRequest) {
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setCommentType(commentCreateRequest.getCommentType());
        comment.setRating(commentCreateRequest.getRating());
        comment.setUser(securityUtil.getCurrentUser());
        comment.setCommentType(commentCreateRequest.getCommentType());
        // 根据评论类型设置商品或商店
        if (commentCreateRequest.getCommentType() == CommentTypeEnum.ITEM) {
            Product product = productRepository.findById(commentCreateRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
            comment.setProduct(product);
        } else if (commentCreateRequest.getCommentType() == CommentTypeEnum.SHOP) {
            Store store = storeRepository.findById(commentCreateRequest.getStoreId())
                    .orElseThrow(() -> new EntityNotFoundException("商店不存在"));
            comment.setStore(store);
        }
        
        comment = commentRepository.save(comment);
        
        // 更新评分
        if (comment.getCommentType() == CommentTypeEnum.ITEM) {
            updateItemRating(comment.getProduct().getId());
        } else if (comment.getCommentType() == CommentTypeEnum.SHOP) {
            updateShopRating(comment.getStore().getId());
        }
    }





    @Override
    public Page<ItemCommentResponse> getItemCommentsPaged(int itemId, int page, int size, String field, boolean order) {
        Sort sort = Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        Pageable pageable = PageRequest.of(page, size, sort);
        return commentRepository.findByProductIdOrderByLikesCountDesc(itemId, pageable)
                .map(this::convertToItemResponse);
    }

    @Override
    public Page<StoreCommentResponse> getShopCommentsPaged(int shopId, int page, int size, String field, boolean order) {
        Sort sort = Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        Pageable pageable = PageRequest.of(page, size, sort);
        return commentRepository.findByStoreIdOrderByLikesCountDesc(shopId, pageable)
                .map(this::convertToStoreResponse);
    }

    @Override
    @Transactional
    public void likeComment(int commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        comment.setLikesCount(comment.getLikesCount() + 1);
        comment = commentRepository.save(comment);
        
    }

    @Override
    @Transactional
    public void replyToComment(int parentId, CommentReplyRequest replyRequest) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("父评论不存在"));
        
        Comment reply = new Comment();
        reply.setContent(replyRequest.getContent());
        reply.setParentCommentId(parentId);
        reply.setCommentType(parentComment.getCommentType());
        reply.setUser(securityUtil.getCurrentUser());
        if (parentComment.getCommentType() == CommentTypeEnum.ITEM) {
            Product product = productRepository.findById(parentComment.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
            reply.setProduct(product);
        } else if (parentComment.getCommentType() == CommentTypeEnum.SHOP) {
            Store store = storeRepository.findById(parentComment.getStore().getId())
                    .orElseThrow(() -> new EntityNotFoundException("商店不存在"));
            reply.setStore(store);
        }
        reply = commentRepository.save(reply);
    }

    @Override
    @Transactional
    public void deleteComment(int commentId, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        if (comment.getUser().getId() != userId) {
            throw new RuntimeException("无权删除该评论");
        }
        commentRepository.softDelete(commentId);
        
        // 更新评分
        if (comment.getCommentType() == CommentTypeEnum.ITEM) {
            updateItemRating(comment.getProduct().getId());
        } else if (comment.getCommentType() == CommentTypeEnum.SHOP) {
            updateShopRating(comment.getStore().getId());
        }
    }

    @Override
    public List<CommentReplyResponse> getReplies(int commentId) {
        return commentRepository.findByCommentIdOrderByLikesCountDesc(commentId)
                .stream()
                .map(this::convertToReplyResponse)
                .collect(Collectors.toList());
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
        return commentRepository.countByProductIdAndIsDeletedFalse(itemId);
    }

    @Override
    public long getShopCommentCount(int shopId) {
        return commentRepository.countByStoreIdAndIsDeletedFalse(shopId);
    }

    @Override
    @Transactional
    public void updateItemRating(int itemId) {
        Double avgRating = commentRepository.getAverageRatingForItem(itemId);
        if (avgRating != null) {
            Product product = productRepository.findById(itemId)
                    .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
            product.setRate(avgRating);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void updateShopRating(int shopId) {
        Double avgRating = commentRepository.getAverageRatingForShop(shopId);
        if (avgRating != null) {
            Store store = storeRepository.findById(shopId)
                    .orElseThrow(() -> new EntityNotFoundException("商店不存在"));
            store.setScore(avgRating.intValue());
            store.setScoreCount((int)commentRepository.countByStoreIdAndIsDeletedFalse(shopId));
            storeRepository.save(store);
        }
    }

    private ItemCommentResponse convertToItemResponse(Comment comment) {
        return new ItemCommentResponse(comment);
    }

    private StoreCommentResponse convertToStoreResponse(Comment comment) {
        return new StoreCommentResponse(comment);
    }

    private CommentReplyResponse convertToReplyResponse(Comment comment) {
        return new CommentReplyResponse(comment);
    }
} 