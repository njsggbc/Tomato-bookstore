package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductUpdateRequest {
    private String title;
    private String description;
    private List<MultipartFile> images;
    private BigDecimal price;
    private Map<String, String> specifications;
}
