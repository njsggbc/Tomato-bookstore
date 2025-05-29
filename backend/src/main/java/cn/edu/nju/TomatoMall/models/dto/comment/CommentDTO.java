package cn.edu.nju.TomatoMall.models.dto.comment;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private int id;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000字符之间")
    private String content;

    private Integer likesCount;

    @NotNull(message = "用户ID不能为空")
    private int userId;

    private int itemId;

    private int shopId;

    @NotNull(message = "评论类型不能为空")
    private CommentTypeEnum commentType;

    private Integer parentId;

    @Min(value = 1, message = "评分必须大于等于1")
    @Max(value = 5, message = "评分必须小于等于5")
    private int rating = 5;

    private boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String username; // 用于显示评论者的用户名

    // 额外的展示字段
    private String parentUserName; // 父评论的用户名
    private String itemName; // 商品名称
    private String shopName; // 商店名称
} 