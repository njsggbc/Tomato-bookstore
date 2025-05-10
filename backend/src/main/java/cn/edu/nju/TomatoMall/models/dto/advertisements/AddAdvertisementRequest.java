package cn.edu.nju.TomatoMall.models.dto.advertisements;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class AddAdvertisementRequest {
    private String title;
    private String content;
    private List<MultipartFile> imgUrls;
    private Integer productId;
}