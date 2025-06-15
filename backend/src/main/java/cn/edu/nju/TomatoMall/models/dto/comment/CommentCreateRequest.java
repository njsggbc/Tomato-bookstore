package cn.edu.nju.TomatoMall.models.dto.comment;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CommentCreateRequest {
    @NotBlank
    private String content;
    private Integer rating;
} 