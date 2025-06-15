package cn.edu.nju.TomatoMall.enums;

import lombok.Getter;

@Getter
public enum CommentTypeEnum {
    ITEM("ITEM", "商品评论"),
    SHOP("SHOP", "商店评论");


    private final String code;
    private final String description;


    CommentTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}