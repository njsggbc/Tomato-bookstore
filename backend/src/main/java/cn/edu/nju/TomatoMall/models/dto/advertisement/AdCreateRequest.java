package cn.edu.nju.TomatoMall.models.dto.advertisement;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AdCreateRequest {
    @NotBlank
    private String title;

    @NotNull
    private MultipartFile content; // 广告图片

    @NotBlank
    private String linkUrl; // 跳转链接

    @NotNull
    private Integer storeId; // 商店ID
}