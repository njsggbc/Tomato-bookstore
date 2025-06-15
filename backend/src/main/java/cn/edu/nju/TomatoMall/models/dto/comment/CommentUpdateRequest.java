package cn.edu.nju.TomatoMall.models.dto.comment;

import lombok.Data;

@Data
public class CommentUpdateRequest {
    private String content;
    private Integer rating;
}
