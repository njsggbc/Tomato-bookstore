package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductUpdateRequest {
    String title;
    String description;
    List<MultipartFile> images;
    BigDecimal price;
    Map<String, String> specifications;
}
