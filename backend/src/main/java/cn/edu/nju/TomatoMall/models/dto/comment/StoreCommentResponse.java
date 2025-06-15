package cn.edu.nju.TomatoMall.models.dto.comment;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import cn.edu.nju.TomatoMall.models.po.Comment;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreCommentResponse {
    private int id;
    private String content;
    private Integer likesCount;
    
    // 用户信息
    private int userId;
    private String username;
    private int parentCommentId;
    // 店铺信息
    private int storeId;
    private String storeName;
    
    private CommentTypeEnum commentType;
    private boolean isDeleted;
    private LocalDateTime createdAt;

    public StoreCommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.likesCount = comment.getLikesCount();
        
        // 设置用户信息
        User user = comment.getUser();
        this.userId = user.getId();
        this.username = user.getUsername();
        
        // 设置店铺信息
        Store store = comment.getStore();
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.parentCommentId = comment.getParentCommentId();
        this.commentType = comment.getCommentType();
        this.isDeleted = comment.isDeleted();
        this.createdAt = comment.getCreatedAt();
    }
}
