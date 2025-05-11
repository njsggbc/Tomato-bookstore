package cn.edu.nju.TomatoMall.models.dto.advertisement;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class AdUpdateRequest {
    private String title;

    private MultipartFile content; // 可选，更新广告图片

    private String linkUrl;
}