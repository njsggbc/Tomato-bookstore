package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Data;
import lombok.NonNull;
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
    @NotNull
    @Size(min = 1)
    private Map<String, String> specifications;
}
