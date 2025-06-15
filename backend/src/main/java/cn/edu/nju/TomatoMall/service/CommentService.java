package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.comment.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {
    void createComment(CommentCreateRequest commentCreateRequest);

    void deleteComment(int commentId, int userId);

    Page<ItemCommentResponse> getItemCommentsPaged(int itemId, int page, int size, String field, boolean order);
    Page<StoreCommentResponse> getShopCommentsPaged(int shopId, int page, int size, String field, boolean order);
    void likeComment(int commentId);
    void replyToComment(int parentId, CommentReplyRequest replyDTO);
    Double getItemAverageRating(int itemId);
    Double getShopAverageRating(int shopId);
    long getItemCommentCount(int itemId);
    long getShopCommentCount(int shopId);
    List<CommentReplyResponse> getReplies(int commentId);
    
    /**
     * 更新商品评分
     * @param itemId 商品ID
     */
    void updateItemRating(int itemId);

    /**
     * 更新商店评分
     * @param shopId 商店ID
     */
    void updateShopRating(int shopId);
} 