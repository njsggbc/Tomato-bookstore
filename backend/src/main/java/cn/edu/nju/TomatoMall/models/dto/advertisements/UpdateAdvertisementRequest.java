package cn.edu.nju.TomatoMall.models.dto.advertisements;
import cn.edu.nju.TomatoMall.models.po.Advertisement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UpdateAdvertisementRequest {

    private Integer id;
    private String title;
    private String content;
    private List<MultipartFile> imageUrls;
    private Integer productId;



}
