package cn.edu.nju.TomatoMall.models.dto.comment;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.po.Comment;
import lombok.Data;

@Data
public class CommentResponse {
    private int id;
    private EntityType entityType;
    private int entityId;
    private Integer parentId;
    private String content;
    private Integer rating;
    private UserBriefResponse author;
    private int likes;
    private boolean liked;
    private String createdAt;
    private String updatedAt;

    public CommentResponse(Comment comment, boolean liked) {
        this.id = comment.getId();
        this.entityType = comment.getEntityType();
        this.entityId = comment.getEntityId();
        this.parentId = comment.getParent() == null ? null : comment.getParent().getId();
        this.content = comment.getContent();
        this.rating = comment.getRating();
        this.author = new UserBriefResponse(comment.getUser());
        this.likes = comment.getLikes();
        this.liked = liked;
        this.createdAt = comment.getCreateTime().toString();
        this.updatedAt = comment.getUpdateTime().toString();
    }
}
