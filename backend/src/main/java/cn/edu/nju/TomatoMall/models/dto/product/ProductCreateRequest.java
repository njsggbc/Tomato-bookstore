package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.util.JsonMapConverter;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    @Size(min = 1)
    private List<MultipartFile> images;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer storeId;
    @NotBlank
    private String specifications; // 序列化JSON字符串

    public Map<String, String> getSpecifications() {
        return JsonMapConverter.jsonToStringMap(specifications);
    }
}
