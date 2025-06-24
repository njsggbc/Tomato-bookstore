package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.util.JsonMapConverter;
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
    private String specifications; // 序列化JSON字符串

    public Map<String, String> getSpecifications() {
        if (specifications == null || specifications.isEmpty()) {
            return null;
        }
        return JsonMapConverter.jsonToStringMap(specifications);
    }
}
