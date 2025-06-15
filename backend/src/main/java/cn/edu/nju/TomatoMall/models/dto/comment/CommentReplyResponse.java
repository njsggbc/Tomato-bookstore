package cn.edu.nju.TomatoMall.models.dto.comment;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import cn.edu.nju.TomatoMall.models.po.Comment;
import cn.edu.nju.TomatoMall.models.po.Product;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyResponse {
    private int commentId;
    private String content;
    private Integer likesCount;
    
    // 用户信息
    private int userId;
    private String username;
    
    // 父评论用户信息
    private int parentCommentId;
    
    // 商品信息（如果有）
    private Integer productId;
    private String itemName;
    
    // 店铺信息
    private Integer storeId;
    private String storeName;
    
    private CommentTypeEnum commentType;
    private boolean isDeleted;
    private LocalDateTime createdAt;

    public CommentReplyResponse(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.likesCount = comment.getLikesCount();
        
        // 设置用户信息
        User user = comment.getUser();
        this.userId = user.getId();
        this.username = user.getUsername();
        
        // 设置父评论用户信息
        this.parentCommentId = comment.getParentCommentId();
        
        // 设置商品信息（如果有）
        Product product = comment.getProduct();
        if (product != null) {
            this.productId = product.getId();
            this.itemName = product.getName();
        }
        
        // 设置店铺信息
        Store store = comment.getStore();
        if (store != null) {
            this.storeId = store.getId();
            this.storeName = store.getName();
        }
        
        this.commentType = comment.getCommentType();
        this.isDeleted = comment.isDeleted();
        this.createdAt = comment.getCreatedAt();
    }

}

