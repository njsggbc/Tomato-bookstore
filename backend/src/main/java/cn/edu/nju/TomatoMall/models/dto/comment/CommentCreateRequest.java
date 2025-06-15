package cn.edu.nju.TomatoMall.models.dto.comment;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CommentCreateRequest {
    private Integer productId;
    private Integer storeId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000字符之间")
    private String content;
    
    @NotNull(message = "评论类型不能为空")
    private CommentTypeEnum commentType;
    
    @Min(value = 1, message = "评分必须大于等于1")
    @Max(value = 5, message = "评分必须小于等于5")
    private int rating = 5;

    @AssertTrue(message = "商品评论必须提供商品ID")
    private boolean isValidProductComment() {
        if (commentType == CommentTypeEnum.ITEM) {
            return productId != null;
        }
        return true;
    }

    @AssertTrue(message = "商店评论必须提供商店ID")
    private boolean isValidStoreComment() {
        if (commentType == CommentTypeEnum.SHOP) {
            return storeId != null;
        }
        return true;
    }
} 